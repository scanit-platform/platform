package com.scanit.receipt.dto;

import com.scanit.receipt.model.OCRStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptDTO(
    Long id,
    String vendorName,
    BigDecimal transactionAmount,
    BigDecimal totalAmount,
    LocalDate transactionDate,
    String imageUrl,
    OCRStatus ocrStatus,
    Long userId
) {}
