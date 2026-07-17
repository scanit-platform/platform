package com.scanit.receipt.service;

import com.scanit.category.service.CategoryReferenceService;
import com.scanit.category.service.CategorySelection;
import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.dto.ReceiptExtractRequestDTO;
import com.scanit.receipt.dto.ReceiptUpdateRequestDTO;
import com.scanit.receipt.exception.ReceiptNotExtractedException;
import com.scanit.receipt.mapper.AnalyzeExpenseResponseMapper;
import com.scanit.receipt.mapper.ReceiptMapper;
import com.scanit.receipt.model.OCRStatus;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.repository.ReceiptRepository;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseRequest;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final UserRepository userRepository;
    private final TextractClient textractClient;
    private final AnalyzeExpenseResponseMapper analyzeExpenseResponseMapper;
    private final S3StorageService s3StorageService;
    private final CategoryReferenceService categoryReferenceService;
    private final String receiptsBucket;

    public ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            ReceiptMapper receiptMapper,
            UserRepository userRepository,
            TextractClient textractClient,
            AnalyzeExpenseResponseMapper analyzeExpenseResponseMapper,
            S3StorageService s3StorageService,
            CategoryReferenceService categoryReferenceService,
            @Value("${spring.cloud.aws.s3.receipts-bucket}") String receiptsBucket
    ) {
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
        this.userRepository = userRepository;
        this.textractClient = textractClient;
        this.analyzeExpenseResponseMapper = analyzeExpenseResponseMapper;
        this.s3StorageService = s3StorageService;
        this.categoryReferenceService = categoryReferenceService;
        this.receiptsBucket = receiptsBucket;
    }

    @Override
    @Transactional
    public ReceiptDTO save(ReceiptDTO dto) {
        Receipt receipt = receiptMapper.toEntity(dto);

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        CategorySelection categorySelection =
                categoryReferenceService.resolveOptionalSelection(
                        dto.userId(),
                        dto.generalCategoryId(),
                        dto.customCategoryId()
                );

        receipt.setUser(user);
        receipt.setGeneralCategory(
                categorySelection.generalCategory()
        );
        receipt.setCustomCategory(
                categorySelection.customCategory()
        );
        receipt.setImageUrl(dto.imageUrl());
        receipt.setOcrStatus(dto.ocrStatus());

        Receipt savedReceipt =
                receiptRepository.save(receipt);

        return receiptMapper.toDTO(savedReceipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> search(
            Long userId,
            String vendorName,
            LocalDate transactionDate
    ) {
        if (userId != null
                && vendorName != null
                && transactionDate != null) {

            return receiptRepository
                    .findByUserIdAndVendorNameAndTransactionDate(
                            userId,
                            vendorName,
                            transactionDate
                    );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId is required"
            );
        }

        if (vendorName != null) {
            return receiptRepository
                    .findByUserIdAndVendorName(
                            userId,
                            vendorName
                    );
        }

        return receiptRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public ReceiptDTO uploadReceipt(
            MultipartFile file,
            Long userId
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Receipt file is required"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        String imageUrl = s3StorageService.upload(file);

        Receipt receipt = new Receipt();
        receipt.setUser(user);
        receipt.setImageUrl(imageUrl);

        receipt.setOcrStatus(OCRStatus.PENDING);

        receipt.setVendorName("Pending OCR");
        receipt.setTransactionDate(LocalDate.now());

        Receipt saved =
                receiptRepository.save(receipt);

        try {
            extractAndUpdate(saved.getId(), new ReceiptExtractRequestDTO(
                    userId,
                    extractKeyFromUrl(saved.getImageUrl())
            ));
        } catch (ReceiptNotExtractedException e) {
            saved.setOcrStatus(OCRStatus.FAILED);
            receiptRepository.save(saved);
            return receiptMapper.toDTO(saved);
        }
        Receipt updated = receiptRepository.findById(saved.getId())
                .orElse(saved);
        return receiptMapper.toDTO(updated);
    }

    @Override
    public Iterable<Receipt> findAll() {
        return receiptRepository.findAll();
    }

    @Override
    public Optional<Receipt> findById(Long id) {
        return receiptRepository.findById(id);
    }

    @Override
    public void deleteByReceiptId(Long id) {
        receiptRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Receipt updateReceipt(Long id, ReceiptUpdateRequestDTO dto) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receipt not found: " + id));

        if (dto.vendorName() != null) receipt.setVendorName(dto.vendorName());
        if (dto.totalAmount() != null) receipt.setTotalAmount(dto.totalAmount());
        if (dto.transactionAmount() != null) receipt.setTransactionAmount(dto.transactionAmount());
        if (dto.transactionDate() != null) receipt.setTransactionDate(dto.transactionDate());

        return receiptRepository.save(receipt);
    }

    @Override
    public List<Receipt> findByVendorName(String vendorName) {
        return receiptRepository.findByVendorName(vendorName);
    }

    @Override
    public List<Receipt> findByTransactionDate(
            LocalDate transactionDate
    ) {
        return receiptRepository
                .findByTransactionDate(transactionDate);
    }

    @Override
    @Transactional(
            propagation = Propagation.NOT_SUPPORTED
    )
    public ReceiptDTO extract(ReceiptExtractRequestDTO dto) {
        userRepository.findById(dto.userId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        String imageUrl = toS3Url(dto.key());

        Receipt receipt = receiptRepository
                .findByUserIdAndImageUrl(
                        dto.userId(),
                        imageUrl
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Uploaded receipt not found"
                        )
                );

        if (mockOcrEnabled) {

            receipt.setVendorName("Lidl");
            receipt.setTotalAmount(new BigDecimal("42.99"));
            receipt.setTransactionAmount(new BigDecimal("38.99"));
            receipt.setTransactionDate(LocalDate.now());
            receipt.setOcrStatus(OCRStatus.COMPLETED);
            return receiptMapper.toDTO(receiptRepository.save(receipt));
        }

        // PENDING → PROCESSING
        receipt.setOcrStatus(OCRStatus.PROCESSING);
        receiptRepository.save(receipt);

        try {
            AnalyzeExpenseRequest request =
                    AnalyzeExpenseRequest.builder()
                            .document(
                                    Document.builder()
                                            .s3Object(
                                                    S3Object.builder()
                                                            .bucket(
                                                                    receiptsBucket
                                                            )
                                                            .name(dto.key())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            AnalyzeExpenseResponse response =
                    textractClient.analyzeExpense(request);

            Receipt extractedReceipt =
                    analyzeExpenseResponseMapper
                            .toEntity(response);

            if (extractedReceipt == null) {
                throw new ReceiptNotExtractedException(
                        "Couldn't extract receipt data"
                );
            }

            /*push data to existed notation */
            receipt.setVendorName(
                    extractedReceipt.getVendorName()
            );

            receipt.setTransactionAmount(
                    extractedReceipt
                            .getTransactionAmount()
            );

            receipt.setTotalAmount(
                    extractedReceipt.getTotalAmount()
            );

            receipt.setTransactionDate(
                    extractedReceipt
                            .getTransactionDate()
            );

            // PROCESSING → COMPLETED
            receipt.setOcrStatus(OCRStatus.COMPLETED);

            ensurePersistableReceipt(receipt);

            Receipt savedReceipt =
                    receiptRepository.save(receipt);

            return receiptMapper.toDTO(savedReceipt);

        } catch (RuntimeException exception) {
            // PROCESSING → FAILED
            receipt.setOcrStatus(OCRStatus.FAILED);
            receiptRepository.save(receipt);

            if (exception
                    instanceof ReceiptNotExtractedException) {

                throw exception;
            }

            throw new ReceiptNotExtractedException(
                    "Couldn't extract receipt data"
            );
        }
    }

    @Value("${app.ocr.mock-enabled:false}")
    private boolean mockOcrEnabled;

    @Override
    @Transactional
    public ReceiptDTO extractAndUpdate(Long receiptId, ReceiptExtractRequestDTO dto) {

        Receipt existing = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt Not Found: " + receiptId));

        if (mockOcrEnabled) {

            existing.setVendorName("Lidl");
            existing.setTotalAmount(new BigDecimal("42.99"));
            existing.setTransactionAmount(new BigDecimal("38.99"));
            existing.setTransactionDate(LocalDate.now());
            existing.setOcrStatus(OCRStatus.COMPLETED);
            return receiptMapper.toDTO(receiptRepository.save(existing));
        }

        AnalyzeExpenseRequest request = AnalyzeExpenseRequest.builder()
                .document(Document.builder()
                        .s3Object(S3Object.builder()
                                .bucket(receiptsBucket)
                                .name(dto.key())
                                .build())
                        .build())
                .build();

        AnalyzeExpenseResponse response = textractClient.analyzeExpense(request);

        Receipt extracted = analyzeExpenseResponseMapper.toEntity(response);

        if (extracted == null) {
            existing.setOcrStatus(OCRStatus.FAILED);
            receiptRepository.save(existing);
            throw new ReceiptNotExtractedException("Couldn't extract receipt data");
        }

        existing.setVendorName(extracted.getVendorName());
        existing.setTransactionAmount(extracted.getTransactionAmount());
        existing.setTotalAmount(extracted.getTotalAmount());
        existing.setTransactionDate(extracted.getTransactionDate());
        existing.setOcrStatus(OCRStatus.COMPLETED);
        existing.setLineItems(extracted.getLineItems());

        existing = receiptRepository.save(existing);
        return receiptMapper.toDTO(existing);
    }

    //Not needed if Key refers to entire Url
    private String extractKeyFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }


    private void ensurePersistableReceipt(Receipt receipt) {
        if (receipt.getVendorName() == null || receipt.getVendorName().isBlank()) {
            receipt.setVendorName("Unknown vendor");
        }

        if (receipt.getTransactionDate() == null) {
            receipt.setTransactionDate(
                    LocalDate.now()
            );
        }

        if (receipt.getOcrStatus() == null) {
            receipt.setOcrStatus(
                    OCRStatus.COMPLETED
            );
        }
    }

    private String toS3Url(String key) {
        return String.format(
                "https://%s.s3.amazonaws.com/%s",
                receiptsBucket,
                key
        );
    }
}