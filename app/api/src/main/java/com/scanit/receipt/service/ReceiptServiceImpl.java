package com.scanit.receipt.service;

import com.scanit.receipt.model.OCRStatus;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.dto.ReceiptDTO;

import java.time.LocalDate;
import java.util.Optional;

import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.scanit.receipt.repository.ReceiptRepository;
import com.scanit.receipt.mapper.ReceiptMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;

    public ReceiptServiceImpl(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper, UserRepository userRepository, S3StorageService s3StorageService) {
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
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
    public List<Receipt> search(Long userId, String vendorName, LocalDate  transactionDate) {
        if (userId != null && vendorName != null && transactionDate != null) {
            return  receiptRepository.findByUserIdAndVendorNameAndTransactionDate(userId, vendorName, transactionDate);
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
        System.out.println("UPLOAD RECEIVED");
        System.out.println("userId = " + userId);
        System.out.println("file = " + file.getOriginalFilename());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        System.out.println("Before s3 upload");
        String imageUrl = s3StorageService.upload(file);
        System.out.println("After s3 upload");

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
}
