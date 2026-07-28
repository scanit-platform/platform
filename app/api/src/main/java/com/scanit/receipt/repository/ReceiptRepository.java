package com.scanit.receipt.repository;

import com.scanit.receipt.model.Receipt;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository
        extends CrudRepository<Receipt, Long> {

    List<Receipt> findByUserId(Long userId);

    List<Receipt> findByUserIdAndVendorName(
            Long userId,
            String vendorName
    );

    List<Receipt> findByVendorName(
            String vendorName
    );

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

    /*
     * Returns all other receipts belonging to the same user
     * that already have a perceptual image hash.
     *
     * The hashes are compared later in DuplicateReceiptService
     * using Hamming distance.
     */
    List<Receipt> findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
            Long userId,
            Long receiptId
    );

    Optional<Receipt> findFirstByUserIdAndVendorNameIgnoreCaseAndTotalAmountAndTransactionDateAndIdNotOrderByIdAsc(
            Long userId,
            String vendorName,
            BigDecimal totalAmount,
            LocalDate transactionDate,
            Long receiptId
    );

    boolean existsByCustomCategoryId(
            UUID customCategoryId
    );
}