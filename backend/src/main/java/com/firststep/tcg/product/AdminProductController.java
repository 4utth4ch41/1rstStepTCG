package com.firststep.tcg.product;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final ProductImageStorage imageStorage;

    public AdminProductController(ProductService productService, ProductImageStorage imageStorage) {
        this.productService = productService;
        this.imageStorage = imageStorage;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody CreateProductRequest request) {

        if (request.stock() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "กรุณาระบุจำนวนสต็อก");
        }

        return productService.createProduct(
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        );
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Product createWithImages(
            @RequestPart("product") CreateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        List<String> urls = imageStorage.store(images == null ? List.of() : images);
        try {
            return productService.createProductWithImages(request, urls);
        } catch (RuntimeException ex) {
            imageStorage.delete(urls);
            throw ex;
        }
    }
}