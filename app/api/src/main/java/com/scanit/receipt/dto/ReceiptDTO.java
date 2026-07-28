package com.scanit.receipt.dto;

import com.scanit.receipt.model.DuplicateMatchReason;
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
        UUID customCategoryId,

        boolean duplicate,
        boolean savedAsDuplicate,
        Long duplicateOfReceiptId,
        DuplicateMatchReason duplicateMatchReason
) {
}