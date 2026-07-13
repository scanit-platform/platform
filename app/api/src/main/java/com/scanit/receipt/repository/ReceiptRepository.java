package com.scanit.receipt.repository;

import com.scanit.receipt.model.Receipt;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends CrudRepository<Receipt, Long> {

    List<Receipt> findByUserId(Long userId);

    List<Receipt> findByUserIdAndVendorName(
            Long userId,
            String vendorName
    );

    List<Receipt> findByVendorName(String vendorName);

    List<Receipt> findByTransactionDate(
            LocalDate transactionDate
    );

    List<Receipt> findByUserIdAndVendorNameAndTransactionDate(
            Long userId,
            String vendorName,
            LocalDate transactionDate
    );

    Optional<Receipt> findByUserIdAndImageUrl(
            Long userId,
            String imageUrl
    );

    boolean existsByCustomCategoryId(UUID customCategoryId);
}