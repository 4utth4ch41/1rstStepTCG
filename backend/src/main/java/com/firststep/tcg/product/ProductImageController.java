package com.firststep.tcg.product;

import java.time.Duration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {
    private final ProductImageStorage storage;
    public ProductImageController(ProductImageStorage storage) { this.storage = storage; }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> image(@PathVariable String filename) {
        Resource image = storage.load(filename);
        return ResponseEntity.ok()
            .contentType(filename.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
            .header("X-Content-Type-Options", "nosniff")
            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
            .body(image);
    }
}
