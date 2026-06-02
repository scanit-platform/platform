package com.scanit.receipt.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceiptDTO(
    Long id,
    String vendorName,
    BigDecimal transactionAmount,
    BigDecimal totalAmount,
    LocalDate transactionDate,
    Long userId
) {}
