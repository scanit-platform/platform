package com.scanit.receipt.service;

import com.scanit.receipt.model.DuplicateMatchReason;
import com.scanit.receipt.model.OCRStatus;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.repository.ReceiptRepository;
import com.scanit.user.model.User;
import com.scanit.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicateReceiptServiceTests {

    private static final String CURRENT_HASH =
            "0000000000000000";

    private static final String SIMILAR_HASH =
            "000000000000001f";

    @Mock
    private ReceiptRepository receiptRepository;

    private DuplicateReceiptService duplicateReceiptService;

    @BeforeEach
    void setUp() {
        duplicateReceiptService =
                new DuplicateReceiptService(
                        receiptRepository,
                        new PerceptualHashService()
                );

        when(receiptRepository.save(
                any(Receipt.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );
    }

    @Test
    void shouldCompleteReceiptWhenNoMatchExists() {
        Receipt current =
                createReceipt(
                        20L,
                        CURRENT_HASH
                );

        when(receiptRepository
                .findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
                        1L,
                        20L
                )
        ).thenReturn(List.of());

        stubDetailsMatch(
                current,
                Optional.empty()
        );

        Receipt result =
                duplicateReceiptService
                        .checkAndMarkDuplicate(current);

        assertThat(result.isDuplicate())
                .isFalse();

        assertThat(result.isSavedAsDuplicate())
                .isFalse();

        assertThat(result.getDuplicateOf())
                .isNull();

        assertThat(result.getDuplicateMatchReason())
                .isNull();

        assertThat(result.getOcrStatus())
                .isEqualTo(OCRStatus.COMPLETED);
    }

    @Test
    void shouldMarkDuplicateForImageMatch() {
        Receipt current =
                createReceipt(
                        20L,
                        CURRENT_HASH
                );

        Receipt existing =
                createReceipt(
                        10L,
                        SIMILAR_HASH
                );

        when(receiptRepository
                .findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
                        1L,
                        20L
                )
        ).thenReturn(List.of(existing));

        stubDetailsMatch(
                current,
                Optional.empty()
        );

        Receipt result =
                duplicateReceiptService
                        .checkAndMarkDuplicate(current);

        assertThat(result.isDuplicate())
                .isTrue();

        assertThat(result.getDuplicateOf())
                .isSameAs(existing);

        assertThat(result.getDuplicateMatchReason())
                .isEqualTo(
                        DuplicateMatchReason.IMAGE_HASH
                );

        assertThat(result.getOcrStatus())
                .isEqualTo(
                        OCRStatus.DUPLICATE_REVIEW
                );
    }

    @Test
    void shouldMarkDuplicateForDetailsMatch() {
        Receipt current =
                createReceipt(
                        20L,
                        CURRENT_HASH
                );

        Receipt existing =
                createReceipt(
                        10L,
                        "ffffffffffffffff"
                );

        when(receiptRepository
                .findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
                        1L,
                        20L
                )
        ).thenReturn(List.of());

        stubDetailsMatch(
                current,
                Optional.of(existing)
        );

        Receipt result =
                duplicateReceiptService
                        .checkAndMarkDuplicate(current);

        assertThat(result.getDuplicateOf())
                .isSameAs(existing);

        assertThat(result.getDuplicateMatchReason())
                .isEqualTo(
                        DuplicateMatchReason.RECEIPT_DETAILS
                );

        assertThat(result.getOcrStatus())
                .isEqualTo(
                        OCRStatus.DUPLICATE_REVIEW
                );
    }

    @Test
    void shouldUseBothWhenChecksFindSameReceipt() {
        Receipt current =
                createReceipt(
                        20L,
                        CURRENT_HASH
                );

        Receipt existing =
                createReceipt(
                        10L,
                        SIMILAR_HASH
                );

        when(receiptRepository
                .findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
                        1L,
                        20L
                )
        ).thenReturn(List.of(existing));

        stubDetailsMatch(
                current,
                Optional.of(existing)
        );

        Receipt result =
                duplicateReceiptService
                        .checkAndMarkDuplicate(current);

        assertThat(result.getDuplicateOf())
                .isSameAs(existing);

        assertThat(result.getDuplicateMatchReason())
                .isEqualTo(
                        DuplicateMatchReason.BOTH
                );
    }

    @Test
    void shouldPreferImageWhenChecksFindDifferentReceipts() {
        Receipt current =
                createReceipt(
                        20L,
                        CURRENT_HASH
                );

        Receipt imageMatch =
                createReceipt(
                        10L,
                        SIMILAR_HASH
                );

        Receipt detailsMatch =
                createReceipt(
                        11L,
                        "ffffffffffffffff"
                );

        when(receiptRepository
                .findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
                        1L,
                        20L
                )
        ).thenReturn(List.of(imageMatch));

        stubDetailsMatch(
                current,
                Optional.of(detailsMatch)
        );

        Receipt result =
                duplicateReceiptService
                        .checkAndMarkDuplicate(current);

        assertThat(result.getDuplicateOf())
                .isSameAs(imageMatch);

        assertThat(result.getDuplicateMatchReason())
                .isEqualTo(
                        DuplicateMatchReason.IMAGE_HASH
                );
    }

    private void stubDetailsMatch(
            Receipt current,
            Optional<Receipt> result
    ) {
        when(receiptRepository
                .findFirstByUserIdAndVendorNameIgnoreCaseAndTotalAmountAndTransactionDateAndIdNotOrderByIdAsc(
                        current.getUser().getId(),
                        current.getVendorName(),
                        current.getTotalAmount(),
                        current.getTransactionDate(),
                        current.getId()
                )
        ).thenReturn(result);
    }

    private Receipt createReceipt(
            Long id,
            String imageHash
    ) {
        Receipt receipt = new Receipt();

        receipt.setId(id);
        receipt.setUser(createUser());

        receipt.setImageUrl(
                "https://test-bucket.s3.amazonaws.com/"
                        + id
        );

        receipt.setImageHash(imageHash);
        receipt.setVendorName("Tesco");

        receipt.setTotalAmount(
                new BigDecimal("22.99")
        );

        receipt.setTransactionDate(
                LocalDate.of(2026, 7, 27)
        );

        receipt.setOcrStatus(
                OCRStatus.COMPLETED
        );

        return receipt;
    }

    private User createUser() {
        return new User(
                1L,
                "Alice",
                "Alice",
                "User",
                "alice@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                LocalDateTime.of(
                        2026,
                        7,
                        27,
                        12,
                        0
                )
        );
    }
}