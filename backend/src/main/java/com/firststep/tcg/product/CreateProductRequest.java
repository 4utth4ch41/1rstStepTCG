package com.firststep.tcg.product;

import java.math.BigDecimal;

public record CreateProductRequest(
        String name,
        String description,
        BigDecimal price,
        Integer stock) {
}