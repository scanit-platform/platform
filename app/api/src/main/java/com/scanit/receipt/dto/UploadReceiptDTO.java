package com.scanit.receipt.dto;

import com.scanit.receipt.model.OCRStatus;

public record UploadReceiptDTO(
        Long receiptId,
        String imageUrl,
        OCRStatus ocrStatus
) {
}
