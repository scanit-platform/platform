package com.scanit.receipt.service;

import com.scanit.category.service.CategoryReferenceService;
import com.scanit.category.service.CategorySelection;
import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.dto.ReceiptExtractRequestDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseRequest;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

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
            @Value("${spring.cloud.aws.s3.receipts-bucket}") String receiptsBucket) {
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
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        CategorySelection categorySelection = categoryReferenceService.resolveOptionalSelection(
                dto.userId(),
                dto.generalCategoryId(),
                dto.customCategoryId());
        receipt.setUser(user);
        receipt.setGeneralCategory(categorySelection.generalCategory());
        receipt.setCustomCategory(categorySelection.customCategory());
        receipt.setImageUrl(dto.imageUrl());
        receipt.setOcrStatus(dto.ocrStatus());
        Receipt savedReceipt = receiptRepository.save(receipt);
        return receiptMapper.toDTO(savedReceipt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Receipt> search(Long userId, String vendorName, LocalDate transactionDate) {
        if (userId != null && vendorName != null && transactionDate != null) {
            return receiptRepository.findByUserIdAndVendorNameAndTransactionDate(userId, vendorName, transactionDate);
        }

        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        if (vendorName != null) {
            return receiptRepository.findByUserIdAndVendorName(userId, vendorName);
        }

        return receiptRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public ReceiptDTO uploadReceipt(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Receipt file is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String imageUrl = s3StorageService.upload(file);

        Receipt receipt = new Receipt();
        receipt.setUser(user);
        receipt.setImageUrl(imageUrl);
        receipt.setOcrStatus(OCRStatus.PENDING);
        receipt.setVendorName("Pending OCR");
        receipt.setTransactionDate(LocalDate.now());

        Receipt saved = receiptRepository.save(receipt);

        return receiptMapper.toDTO(saved);
    }

    @Override
    public Iterable<Receipt> findAll() {
        return (List<Receipt>) receiptRepository.findAll();
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
    public List<Receipt> findByVendorName(String vendorName) {
        return receiptRepository.findByVendorName(vendorName);
    }

    @Override
    public List<Receipt> findByTransactionDate(java.time.LocalDate transactionDate) {
        return receiptRepository.findByTransactionDate(transactionDate);
    }

    @Override
    @Transactional
    public ReceiptDTO extract(ReceiptExtractRequestDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AnalyzeExpenseRequest request = AnalyzeExpenseRequest.builder().document(
                Document.builder().s3Object(S3Object.builder().bucket(receiptsBucket).name(dto.key()).build()).build()).build();
        
        AnalyzeExpenseResponse response = textractClient.analyzeExpense(request);
        
        Receipt receipt = analyzeExpenseResponseMapper.toEntity(response);
        
        if (receipt == null) {
            throw new ReceiptNotExtractedException("Couldn't extract receipt data");
        }
        
        receipt.setUser(user);
        receipt.setImageUrl(toS3Url(dto.key()));
        ensurePersistableReceipt(receipt);
        receipt = receiptRepository.save(receipt);

        return receiptMapper.toDTO(receipt);
    }

    private void ensurePersistableReceipt(Receipt receipt) {
        if (receipt.getVendorName() == null || receipt.getVendorName().isBlank()) {
            receipt.setVendorName("Unknown vendor");
        }

        if (receipt.getTransactionDate() == null) {
            receipt.setTransactionDate(LocalDate.now());
        }

        if (receipt.getOcrStatus() == null) {
            receipt.setOcrStatus(OCRStatus.COMPLETED);
        }
    }

    private String toS3Url(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", receiptsBucket, key);
    }
}
