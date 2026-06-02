package com.scanit.receipt.service;

import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.dto.ReceiptDTO;
import java.util.Optional;
import com.scanit.user.repository.User2Repository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.scanit.receipt.repository.ReceiptRepository;
import com.scanit.receipt.mapper.ReceiptMapper;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;
    private final User2Repository user2Repository;

    @Override
    public ReceiptDTO save(ReceiptDTO dto) {
        Receipt receipt = receiptMapper.toEntity(dto);
        user2Repository.findById(dto.userId()).ifPresent(receipt::setUser);
        Receipt savedReceipt = receiptRepository.save(receipt);
        return receiptMapper.toDTO(savedReceipt);
    }

    @Override
    public Optional<Receipt> deleteById(Long id) {
        Optional<Receipt> receipt = receiptRepository.findById(id);
        receipt.ifPresent(receiptRepository::delete);
        return receipt;
    }

    @Override
    public List<Receipt> findAll() {
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
