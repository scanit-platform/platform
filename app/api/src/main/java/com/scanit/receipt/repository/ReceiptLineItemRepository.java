package com.scanit.receipt.repository;

import com.scanit.receipt.model.ReceiptLineItem;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ReceiptLineItemRepository extends CrudRepository<ReceiptLineItem, Long> {
    boolean existsByCustomCategoryId(UUID customCategoryId);
}
