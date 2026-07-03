package com.scanit.receipt.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReceiptLineItemDTO(
        Long id,
        String description,
        Integer quantity,
        BigDecimal price,
        UUID categoryId,
        String category
) { }
