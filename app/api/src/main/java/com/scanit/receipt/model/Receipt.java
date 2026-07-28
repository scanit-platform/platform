package com.scanit.receipt.model;

import com.scanit.category.model.CustomCategory;
import com.scanit.category.model.GeneralCategory;
import com.scanit.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipts")
@Getter
@Setter
@NoArgsConstructor
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String vendorName;

    /** Represents the sub-total before tax */
    @Column(precision = 10, scale = 2)
    private BigDecimal transactionAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = true)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false)
    private OCRStatus ocrStatus;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_hash", length = 64)
    private String imageHash;

    @Column(name = "is_duplicate", nullable = false)
    private boolean duplicate = false;

    @Column(name = "saved_as_duplicate", nullable = false)
    private boolean savedAsDuplicate = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "duplicate_match_reason", length = 30)
    private DuplicateMatchReason duplicateMatchReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_receipt_id")
    private Receipt duplicateOf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "general_category_id")
    private GeneralCategory generalCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_category_id")
    private CustomCategory customCategory;

    @OneToMany(
            mappedBy = "receipt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ReceiptLineItem> lineItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
