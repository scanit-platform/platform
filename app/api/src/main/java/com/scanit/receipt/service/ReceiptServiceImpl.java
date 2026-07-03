package com.scanit.receipt.service;

import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.dto.ReceiptExtractRequestDTO;
import com.scanit.receipt.exception.ReceiptNotExtractedException;

import java.time.LocalDate;
import java.util.Optional;

import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;

import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseRequest;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.scanit.receipt.repository.ReceiptRepository;
import com.scanit.receipt.mapper.AnalyzeExpenseResponseMapper;
import com.scanit.receipt.mapper.ReceiptMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final UserRepository userRepository;
    private final TextractClient textractClient;
    private final AnalyzeExpenseResponseMapper analyzeExpenseResponseMapper;
    
    @Value("${spring.cloud.aws.s3.receipts-bucket")
    private String receiptsBucket;

    public ReceiptServiceImpl(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper,  UserRepository userRepository, TextractClient textractClient, AnalyzeExpenseResponseMapper analyzeExpenseResponseMapper) {
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
        this.userRepository = userRepository;
        this.textractClient = textractClient;
        this.analyzeExpenseResponseMapper = analyzeExpenseResponseMapper;
    }

    @Override
    public ReceiptDTO save(ReceiptDTO dto) {
        Receipt receipt = receiptMapper.toEntity(dto);
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        receipt.setUser(user);
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
        receipt = receiptRepository.save(receipt);

        return receiptMapper.toDTO(receipt);
    }
}
