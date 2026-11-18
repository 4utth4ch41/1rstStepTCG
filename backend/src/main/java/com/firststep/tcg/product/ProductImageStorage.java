package com.firststep.tcg.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductImageStorage {
    private static final String PREFIX = "/api/product-images/";
    private final Path directory;

    public ProductImageStorage(@Value("${app.product-images.directory:./data/product-images}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public List<String> store(List<MultipartFile> files) {
        if (files.size() > 5) throw badImage("เพิ่มรูปได้สูงสุด 5 รูปต่อสินค้า");
        List<String> urls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
                    throw badImage("รูปต้องไม่ว่างและมีขนาดไม่เกิน 5 MB");
                }
                // Inspect actual image bytes, rather than trusting extensions or MIME headers.
                try (var stream = file.getInputStream();
                     var input = ImageIO.createImageInputStream(stream)) {
                    if (input == null) throw badImage("อ่านรูปไม่ได้ กรุณาเลือก JPEG หรือ PNG");
                    var readers = ImageIO.getImageReaders(input);
                    if (!readers.hasNext()) throw badImage("รองรับเฉพาะไฟล์รูป JPEG และ PNG");
                    ImageReader reader = readers.next();
                    try {
                        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                        if (!format.equals("jpeg") && !format.equals("png")) throw badImage("รองรับเฉพาะ JPEG และ PNG");
                        reader.setInput(input, true, true);
                        int width = reader.getWidth(0), height = reader.getHeight(0);
                        if (width <= 0 || height <= 0 || (long) width * height > 20_000_000) {
                            throw badImage("รูปต้องมีความละเอียดไม่เกิน 20 ล้านพิกเซล");
                        }
                        var image = reader.read(0);
                        String filename = UUID.randomUUID() + (format.equals("jpeg") ? ".jpg" : ".png");
                        Files.createDirectories(directory);
                        urls.add(PREFIX + filename);
                        // Re-encode to discard metadata and extra content from the uploaded file.
                        if (!ImageIO.write(image, format, directory.resolve(filename).toFile())) {
                            throw new IOException("No image writer");
                        }
                    } finally { reader.dispose(); }
                }
            }
            return urls;
        } catch (ResponseStatusException ex) {
            delete(urls);
            throw ex;
        } catch (IOException | RuntimeException ex) {
            delete(urls);
            if (ex instanceof IOException && !Files.isDirectory(directory)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "บันทึกรูปไม่สำเร็จ กรุณาลองใหม่");
            }
            throw badImage("ไม่สามารถอ่านหรือบันทึกรูปได้ กรุณาเลือกไฟล์ใหม่");
        }
    }

    public Resource load(String filename) {
        if (!filename.matches("[a-f0-9-]{36}\\.(jpg|png)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path file = directory.resolve(filename);
        if (!Files.isRegularFile(file)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return new FileSystemResource(file);
    }

    public void delete(List<String> urls) {
        for (String url : urls) {
            if (!url.startsWith(PREFIX)) continue;
            String filename = url.substring(PREFIX.length());
            if (!filename.matches("[a-f0-9-]{36}\\.(jpg|png)")) continue;
            try { Files.deleteIfExists(directory.resolve(filename)); }
            catch (IOException ex) { LoggerFactory.getLogger(getClass()).warn("Could not remove unused image {}", filename); }
        }
    }

    private ResponseStatusException badImage(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
