package com.scanit.receipt.controller;

import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.dto.ReceiptExtractRequestDTO;
import com.scanit.receipt.dto.ReceiptUpdateRequestDTO;
import com.scanit.receipt.exception.ReceiptNotFoundException;
import com.scanit.receipt.mapper.ReceiptMapper;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.service.ReceiptService;
import com.scanit.receipt.service.S3StorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@CrossOrigin
@SecurityRequirement(name = "bearerAuth")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final S3StorageService s3StorageService;
    private final ReceiptMapper receiptMapper;

    public ReceiptController(
            ReceiptService receiptService,
            S3StorageService s3StorageService,
            ReceiptMapper receiptMapper
    ) {
        this.receiptService = receiptService;
        this.s3StorageService = s3StorageService;
        this.receiptMapper = receiptMapper;
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createReceipt(
            @RequestBody ReceiptDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptService.save(dto));
    }

    @GetMapping
    public List<ReceiptDTO> findAll() {
        List<ReceiptDTO> dtos = new ArrayList<>();

        for (Receipt receipt : receiptService.findAll()) {
            dtos.add(receiptMapper.toDTO(receipt));
        }

        return dtos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDTO> getById(
            @PathVariable Long id
    ) {
        Receipt receipt = receiptService.findById(id)
                .orElseThrow(() ->
                        new ReceiptNotFoundException(id)
                );

        return ResponseEntity.ok(
                receiptMapper.toDTO(receipt)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id
    ) {
        receiptService.deleteByReceiptId(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/save-as-new")
    public ResponseEntity<ReceiptDTO> saveDuplicateAsNew(
            @PathVariable Long id
    ) {
        ReceiptDTO savedReceipt =
                receiptService.saveDuplicateAsNew(id);

        return ResponseEntity.ok(savedReceipt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiptDTO> updateReceipt(
            @PathVariable Long id,
            @RequestBody ReceiptUpdateRequestDTO dto
    ) {
        Receipt updated =
                receiptService.updateReceipt(id, dto);

        return ResponseEntity.ok(
                receiptMapper.toDTO(updated)
        );
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ReceiptDTO> uploadReceipt(
            @RequestParam("userId") Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        ReceiptDTO uploadedReceipt =
                receiptService.uploadReceipt(
                        file,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(uploadedReceipt);
    }

    @GetMapping("/{receiptId}/download")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long receiptId
    ) {
        Receipt receipt =
                receiptService.findById(receiptId)
                        .orElseThrow(() ->
                                new ReceiptNotFoundException(
                                        receiptId
                                )
                        );

        byte[] file =
                s3StorageService.downloadReceipt(
                        receipt.getImageUrl()
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt.jpg"
                )
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    @GetMapping("/search")
    public List<ReceiptDTO> search(
            @RequestParam Long userId,
            @RequestParam(required = false)
            String vendorName,
            @RequestParam(required = false)
            LocalDate transactionDate
    ) {
        List<Receipt> receipts =
                receiptService.search(
                        userId,
                        vendorName,
                        transactionDate
                );

        List<ReceiptDTO> dtos = new ArrayList<>();

        for (Receipt receipt : receipts) {
            dtos.add(receiptMapper.toDTO(receipt));
        }

        return dtos;
    }

    @PostMapping("/extract")
    public ResponseEntity<ReceiptDTO> extractReceipt(
            @RequestBody ReceiptExtractRequestDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptService.extract(dto));
    }

    @PutMapping("/{id}/extract")
    public ResponseEntity<ReceiptDTO> extractAndUpdate(
            @PathVariable Long id,
            @RequestBody ReceiptExtractRequestDTO dto
    ) {
        return ResponseEntity.ok(
                receiptService.extractAndUpdate(id, dto)
        );
    }

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<String>
    handleReceiptNotFoundException(
            ReceiptNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
}