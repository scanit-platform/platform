package com.scanit.receipt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scanit.receipt.model.OCRStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptDTO(
    Long id,
    String vendorName,
    BigDecimal transactionAmount,
    BigDecimal totalAmount,
    LocalDate transactionDate,
    String imageUrl,
    OCRStatus ocrStatus,
    Long userId,
    UUID generalCategoryId,
    UUID customCategoryId
) {}
