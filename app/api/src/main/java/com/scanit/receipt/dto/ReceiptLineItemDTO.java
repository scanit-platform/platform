package com.scanit.receipt.dto;

import java.math.BigDecimal;

public record ReceiptLineItemDTO(
        Long id,
        String description,
        Integer quantity,
        BigDecimal price,
        String category
) {}
