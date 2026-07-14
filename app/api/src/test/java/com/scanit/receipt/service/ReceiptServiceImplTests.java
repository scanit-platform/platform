package com.scanit.receipt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.scanit.category.service.CategoryReferenceService;
import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.dto.ReceiptExtractRequestDTO;
import com.scanit.receipt.exception.ReceiptNotExtractedException;
import com.scanit.receipt.mapper.AnalyzeExpenseResponseMapper;
import com.scanit.receipt.mapper.ReceiptMapper;
import com.scanit.receipt.model.OCRStatus;
import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.repository.ReceiptRepository;
import com.scanit.user.model.User;
import com.scanit.user.model.UserStatus;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseRequest;
import software.amazon.awssdk.services.textract.model.AnalyzeExpenseResponse;

@ExtendWith(MockitoExtension.class)
public class ReceiptServiceImplTests {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private ReceiptMapper receiptMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TextractClient textractClient;

    @Mock
    private AnalyzeExpenseResponseMapper analyzeExpenseResponseMapper;

    @Mock
    private S3StorageService s3StorageService;

    @Mock
    private CategoryReferenceService categoryReferenceService;

    private ReceiptServiceImpl receiptService;

    @BeforeEach
    void setUp() {
        receiptService = new ReceiptServiceImpl(
                receiptRepository,
                receiptMapper,
                userRepository,
                textractClient,
                analyzeExpenseResponseMapper,
                s3StorageService,
                categoryReferenceService,
                "test-bucket"
        );
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        ReceiptExtractRequestDTO dto =
                new ReceiptExtractRequestDTO(
                        1L,
                        "test-receipt.png"
                );

        assertThatThrownBy(() -> receiptService.extract(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verifyNoInteractions(
                textractClient,
                receiptRepository,
                receiptMapper,
                analyzeExpenseResponseMapper
        );
    }

    @Test
    void shouldSetReceiptToFailedWhenReceiptNotExtracted() {
        User user = createMockUser();

        String key = "test-receipt.png";
        String imageUrl =
                "https://test-bucket.s3.amazonaws.com/" + key;

        Receipt existingReceipt =
                createPendingReceipt(user, imageUrl);

        AnalyzeExpenseResponse response =
                AnalyzeExpenseResponse.builder().build();

        List<OCRStatus> savedStatuses =
                new ArrayList<>();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(receiptRepository.findByUserIdAndImageUrl(
                1L,
                imageUrl
        )).thenReturn(Optional.of(existingReceipt));

        when(receiptRepository.save(any(Receipt.class)))
                .thenAnswer(invocation -> {
                    Receipt savedReceipt =
                            invocation.getArgument(0);

                    savedStatuses.add(
                            savedReceipt.getOcrStatus()
                    );

                    return savedReceipt;
                });

        when(textractClient.analyzeExpense(
                any(AnalyzeExpenseRequest.class)
        )).thenReturn(response);

        when(analyzeExpenseResponseMapper.toEntity(response))
                .thenReturn(null);

        ReceiptExtractRequestDTO dto =
                new ReceiptExtractRequestDTO(1L, key);

        assertThatThrownBy(() -> receiptService.extract(dto))
                .isInstanceOf(
                        ReceiptNotExtractedException.class
                )
                .hasMessage(
                        "Couldn't extract receipt data"
                );

        assertThat(savedStatuses).containsExactly(
                OCRStatus.PROCESSING,
                OCRStatus.FAILED
        );

        assertThat(existingReceipt.getOcrStatus())
                .isEqualTo(OCRStatus.FAILED);

        verifyNoInteractions(receiptMapper);
    }

    @Test
    void shouldUpdateExistingReceiptWhenExtractionSucceeds() {
        User user = createMockUser();

        String key = "test-receipt.png";
        String imageUrl =
                "https://test-bucket.s3.amazonaws.com/" + key;

        Receipt existingReceipt =
                createPendingReceipt(user, imageUrl);

        existingReceipt.setId(1L);

        Receipt extractedReceipt =
                createMockReceipt();

        ReceiptDTO expectedDto =
                createMockReceiptDTO();

        AnalyzeExpenseResponse response =
                AnalyzeExpenseResponse.builder().build();

        List<OCRStatus> savedStatuses =
                new ArrayList<>();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(receiptRepository.findByUserIdAndImageUrl(
                1L,
                imageUrl
        )).thenReturn(Optional.of(existingReceipt));

        when(receiptRepository.save(any(Receipt.class)))
                .thenAnswer(invocation -> {
                    Receipt savedReceipt =
                            invocation.getArgument(0);

                    savedStatuses.add(
                            savedReceipt.getOcrStatus()
                    );

                    return savedReceipt;
                });

        when(textractClient.analyzeExpense(
                any(AnalyzeExpenseRequest.class)
        )).thenReturn(response);

        when(analyzeExpenseResponseMapper.toEntity(response))
                .thenReturn(extractedReceipt);

        when(receiptMapper.toDTO(existingReceipt))
                .thenReturn(expectedDto);

        ReceiptExtractRequestDTO dto =
                new ReceiptExtractRequestDTO(1L, key);

        ReceiptDTO resultDto =
                receiptService.extract(dto);

        assertThat(resultDto)
                .isEqualTo(expectedDto);

        assertThat(existingReceipt.getOcrStatus())
                .isEqualTo(OCRStatus.COMPLETED);

        assertThat(existingReceipt.getVendorName())
                .isEqualTo(
                        extractedReceipt.getVendorName()
                );

        assertThat(existingReceipt.getTransactionAmount())
                .isEqualTo(
                        extractedReceipt
                                .getTransactionAmount()
                );

        assertThat(existingReceipt.getTotalAmount())
                .isEqualTo(
                        extractedReceipt.getTotalAmount()
                );

        assertThat(existingReceipt.getTransactionDate())
                .isEqualTo(
                        extractedReceipt
                                .getTransactionDate()
                );

        /*
         * URL должен остаться у уже существующей записи.
         */
        assertThat(existingReceipt.getImageUrl())
                .isEqualTo(imageUrl);

        assertThat(savedStatuses).containsExactly(
                OCRStatus.PROCESSING,
                OCRStatus.COMPLETED
        );

        ArgumentCaptor<AnalyzeExpenseRequest> captor =
                ArgumentCaptor.forClass(
                        AnalyzeExpenseRequest.class
                );

        verify(textractClient)
                .analyzeExpense(captor.capture());

        assertThat(
                captor.getValue()
                        .document()
                        .s3Object()
                        .bucket()
        ).isEqualTo("test-bucket");

        assertThat(
                captor.getValue()
                        .document()
                        .s3Object()
                        .name()
        ).isEqualTo(key);
    }

    private User createMockUser() {
        return new User(
                1L,
                "Alice",
                "Alice",
                "User",
                "alice@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    private Receipt createPendingReceipt(
            User user,
            String imageUrl
    ) {
        Receipt receipt = new Receipt();

        receipt.setUser(user);
        receipt.setImageUrl(imageUrl);
        receipt.setVendorName("Pending OCR");
        receipt.setTransactionDate(LocalDate.now());
        receipt.setOcrStatus(OCRStatus.PENDING);

        return receipt;
    }

    private Receipt createMockReceipt() {
        Receipt receipt = new Receipt();

        receipt.setVendorName("Tesco");
        receipt.setTransactionAmount(
                BigDecimal.valueOf(21.99)
        );
        receipt.setTotalAmount(
                BigDecimal.valueOf(22.99)
        );
        receipt.setTransactionDate(LocalDate.now());
        receipt.setImageUrl("https://google.com");
        receipt.setOcrStatus(OCRStatus.COMPLETED);

        return receipt;
    }

    private ReceiptDTO createMockReceiptDTO() {
        return new ReceiptDTO(
                1L,
                "Tesco",
                BigDecimal.valueOf(21.99),
                BigDecimal.valueOf(22.99),
                LocalDate.now(),
                "https://test-bucket.s3.amazonaws.com/test-receipt.png",
                OCRStatus.COMPLETED,
                1L,
                null,
                null
        );
    }
}