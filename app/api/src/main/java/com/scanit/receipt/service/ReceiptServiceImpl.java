package com.scanit.receipt.service;

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

import java.util.List;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final UserRepository userRepository;

    public ReceiptServiceImpl(ReceiptRepository receiptRepository, ReceiptMapper receiptMapper,  UserRepository userRepository) {
        this.receiptRepository = receiptRepository;
        this.receiptMapper = receiptMapper;
        this.userRepository = userRepository;
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
}
