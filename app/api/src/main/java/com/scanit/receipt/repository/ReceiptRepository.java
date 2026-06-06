package com.scanit.receipt.repository;

import org.springframework.data.repository.CrudRepository;
import com.scanit.receipt.model.Receipt;
import java.util.List;
import java.time.LocalDate;

public interface ReceiptRepository extends CrudRepository<Receipt, Long> {
    List<Receipt> findByUserId(Long userId);
    List<Receipt> findByUserIdAndVendorName(Long userId, String vendorName);
    List<Receipt> findByVendorName(String vendorName);
    List<Receipt> findByTransactionDate(LocalDate transactionDate);
    List<Receipt> findByUserIdAndVendorNameAndTransactionDate(Long userId, String vendorName, LocalDate transactionDate);
}
