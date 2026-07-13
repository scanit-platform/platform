package com.scanit.receipt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptUpdateRequestDTO (
        String vendorName,
        BigDecimal totalAmount,
        BigDecimal transactionAmount,
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate transactionDate
) {}
