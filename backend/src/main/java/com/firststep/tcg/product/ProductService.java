package com.firststep.tcg.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional
    public Product createProduct(
            String name,
            String description,
            BigDecimal price,
            int stock) {

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "กรุณาระบุชื่อสินค้า");
        }

        String cleanedName = name.strip();

        if (cleanedName.length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ชื่อสินค้าต้องไม่เกิน 200 ตัวอักษร");
        }

        if (description != null && description.length() > 5000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "รายละเอียดต้องไม่เกิน 5000 ตัวอักษร");
        }

        if (price == null || price.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ราคาต้องเป็นศูนย์หรือมากกว่า");
        }

        if (price.stripTrailingZeros().scale() > 2
                || price.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ราคาต้องไม่เกิน 9,999,999,999.99 บาท และมีทศนิยมไม่เกิน 2 ตำแหน่ง");
        }

        if (stock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "สต็อกต้องไม่ติดลบ");
        }

        Product product = new Product(
                cleanedName,
                description,
                price,
                stock
        );

        return productRepository.save(product);
    }
    @Transactional
    public Product createProductWithImages(CreateProductRequest request, List<String> imageUrls) {
        if (request.stock() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "กรุณาระบุจำนวนสต็อก");
        }
        Product product = createProduct(request.name(), request.description(), request.price(), request.stock());
        product.addImages(imageUrls);
        return product;
    }
}