package com.scanit.receipt.controller;

import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.exception.ReceiptNotFoundException;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@CrossOrigin
public class ReceiptController {
    public final ReceiptService receiptService;

    private ReceiptDTO toDTO(Receipt receipt) {
        return new ReceiptDTO(
                receipt.getId(),
                receipt.getVendorName(),
                receipt.getTransactionAmount(),
                receipt.getTotalAmount(),
                receipt.getTransactionDate(),
                receipt.getImageUrl(),
                receipt.getOcrStatus(),
                receipt.getUser().getId()
        );
    }

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createReceipt(@RequestBody ReceiptDTO dto) {
        return ResponseEntity.status(201)
                .body(receiptService.save(dto));
    }

    @GetMapping
    public List<ReceiptDTO> findAll() {
        List<ReceiptDTO> dtos =  new ArrayList<>();

        for (Receipt receipt : receiptService.findAll()) {
            dtos.add(toDTO(receipt));
        }
        return  dtos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDTO> getById(@PathVariable Long id) {
        Receipt receipt = receiptService.findById(id)
                .orElseThrow(() ->
                        new ReceiptNotFoundException(id));

        return ResponseEntity.ok(toDTO(receipt));
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        receiptService.deleteByReceiptId(id);
    }

    @GetMapping("/search")
    public List<ReceiptDTO> search(
            @RequestParam Long userId,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) LocalDate transactionDate
    ) {
        List<Receipt> receipts = receiptService.search(userId, vendorName, transactionDate);
        List<ReceiptDTO> dtos =  new ArrayList<>();

        for (Receipt receipt : receipts) { //uses the receipts variable defined above.
            dtos.add(toDTO(receipt));
        }

        return dtos;
    }

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<String> handleReceiptNotFoundException(ReceiptNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
