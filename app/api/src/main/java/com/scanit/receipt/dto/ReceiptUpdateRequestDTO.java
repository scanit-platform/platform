package com.scanit.receipt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptUpdateRequestDTO (
        String vendorName,
        BigDecimal totalAmount,
        BigDecimal transactionAmount,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate transactionDate,
        UUID generalCategoryId,
        UUID customCategoryId
) {}
