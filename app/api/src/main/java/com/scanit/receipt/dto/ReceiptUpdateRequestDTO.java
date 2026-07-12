package com.scanit.receipt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptUpdateRequestDTO (
        String vendorName,
        BigDecimal totalAmount,
        BigDecimal transactionAmount,
        LocalDate transactionDate
) {}
