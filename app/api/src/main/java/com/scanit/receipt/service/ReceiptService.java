package com.scanit.receipt.service;

import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.dto.ReceiptDTO;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

public interface ReceiptService {
    ReceiptDTO save(ReceiptDTO dto);
    Iterable<Receipt> findAll();
    Optional<Receipt> findById(Long id);
    void deleteByReceiptId(Long id);
    List<Receipt> search(Long id, String vendorName, LocalDate transactionDate);
    List<Receipt> findByVendorName(String vendorName);
    List<Receipt> findByTransactionDate(LocalDate transactionDate);
}
