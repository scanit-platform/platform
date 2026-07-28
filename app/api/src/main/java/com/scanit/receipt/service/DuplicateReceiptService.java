package com.scanit.receipt.service;

import com.scanit.receipt.model.DuplicateMatchReason;
import com.scanit.receipt.model.OCRStatus;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.repository.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Optional;

@Service
@Transactional
public class DuplicateReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PerceptualHashService perceptualHashService;

    public DuplicateReceiptService(
            ReceiptRepository receiptRepository,
            PerceptualHashService perceptualHashService
    ) {
        this.receiptRepository = receiptRepository;
        this.perceptualHashService =
                perceptualHashService;
    }

    public Receipt checkAndMarkDuplicate(
            Receipt receipt
    ) {
        validateReceipt(receipt);

        Optional<Receipt> imageMatch =
                findImageMatch(receipt);

        Optional<Receipt> detailsMatch =
                findDetailsMatch(receipt);

        Optional<DuplicateMatch> duplicateMatch =
                resolveDuplicateMatch(
                        imageMatch,
                        detailsMatch
                );

        if (duplicateMatch.isEmpty()) {
            markAsNotDuplicate(receipt);

            return receiptRepository.save(receipt);
        }

        DuplicateMatch match =
                duplicateMatch.orElseThrow();

        receipt.setDuplicate(true);
        receipt.setSavedAsDuplicate(false);
        receipt.setDuplicateOf(match.receipt());
        receipt.setDuplicateMatchReason(
                match.reason()
        );
        receipt.setOcrStatus(
                OCRStatus.DUPLICATE_REVIEW
        );

        return receiptRepository.save(receipt);
    }

    private Optional<Receipt> findImageMatch(
            Receipt receipt
    ) {
        if (receipt.getImageHash() == null
                || receipt.getImageHash().isBlank()) {

            return Optional.empty();
        }

        return receiptRepository
                .findByUserIdAndImageHashIsNotNullAndIdNotOrderByIdAsc(
                        receipt.getUser().getId(),
                        receipt.getId()
                )
                .stream()
                .filter(candidate ->
                        perceptualHashService.isSimilar(
                                receipt.getImageHash(),
                                candidate.getImageHash()
                        )
                )
                .min(
                        Comparator.comparingInt(candidate ->
                                perceptualHashService
                                        .calculateHammingDistance(
                                                receipt.getImageHash(),
                                                candidate.getImageHash()
                                        )
                        )
                );
    }

    private Optional<Receipt> findDetailsMatch(
            Receipt receipt
    ) {
        if (receipt.getVendorName() == null
                || receipt.getVendorName().isBlank()
                || receipt.getTotalAmount() == null
                || receipt.getTransactionDate() == null) {

            return Optional.empty();
        }

        return receiptRepository
                .findFirstByUserIdAndVendorNameIgnoreCaseAndTotalAmountAndTransactionDateAndIdNotOrderByIdAsc(
                        receipt.getUser().getId(),
                        receipt.getVendorName().trim(),
                        receipt.getTotalAmount(),
                        receipt.getTransactionDate(),
                        receipt.getId()
                );
    }

    private Optional<DuplicateMatch> resolveDuplicateMatch(
            Optional<Receipt> imageMatch,
            Optional<Receipt> detailsMatch
    ) {
        if (imageMatch.isEmpty()
                && detailsMatch.isEmpty()) {

            return Optional.empty();
        }

        if (imageMatch.isPresent()
                && detailsMatch.isPresent()) {

            Receipt imageReceipt =
                    imageMatch.orElseThrow();

            Receipt detailsReceipt =
                    detailsMatch.orElseThrow();

            if (imageReceipt.getId().equals(
                    detailsReceipt.getId()
            )) {
                return Optional.of(
                        new DuplicateMatch(
                                imageReceipt,
                                DuplicateMatchReason.BOTH
                        )
                );
            }

            /*
             * The image hash and receipt details matched
             * two different existing receipts.
             *
             * Image similarity is treated as the stronger
             * duplicate signal in this conflict.
             */
            return Optional.of(
                    new DuplicateMatch(
                            imageReceipt,
                            DuplicateMatchReason.IMAGE_HASH
                    )
            );
        }

        if (imageMatch.isPresent()) {
            return Optional.of(
                    new DuplicateMatch(
                            imageMatch.orElseThrow(),
                            DuplicateMatchReason.IMAGE_HASH
                    )
            );
        }

        return Optional.of(
                new DuplicateMatch(
                        detailsMatch.orElseThrow(),
                        DuplicateMatchReason.RECEIPT_DETAILS
                )
        );
    }

    private void markAsNotDuplicate(
            Receipt receipt
    ) {
        receipt.setDuplicate(false);
        receipt.setSavedAsDuplicate(false);
        receipt.setDuplicateOf(null);
        receipt.setDuplicateMatchReason(null);
        receipt.setOcrStatus(
                OCRStatus.COMPLETED
        );
    }

    private void validateReceipt(
            Receipt receipt
    ) {
        if (receipt.getId() == null) {
            throw new IllegalArgumentException(
                    "Receipt must be saved before duplicate detection"
            );
        }

        if (receipt.getUser() == null
                || receipt.getUser().getId() == null) {

            throw new IllegalArgumentException(
                    "Receipt must belong to a user"
            );
        }
    }

    private record DuplicateMatch(
            Receipt receipt,
            DuplicateMatchReason reason
    ) {
    }
}