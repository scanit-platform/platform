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
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    private ReceiptServiceImpl receiptService;
    
    @BeforeEach
    void setUp() {
        receiptService = new ReceiptServiceImpl(receiptRepository, receiptMapper, userRepository, textractClient, analyzeExpenseResponseMapper);
        ReflectionTestUtils.setField(receiptService, "receiptsBucket", "test-bucket");
    }
    
    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            ReceiptExtractRequestDTO dto = new ReceiptExtractRequestDTO(1L, "test-receipt.png");
            receiptService.extract(dto);
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("User not found");

        verifyNoInteractions(textractClient, receiptRepository, receiptMapper, analyzeExpenseResponseMapper);
    }
    
    @Test
    void shouldThrowExceptionWhenReceiptNotExtracted() {
        User user = createMockUser();
        AnalyzeExpenseResponse response = AnalyzeExpenseResponse.builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(textractClient.analyzeExpense(any(AnalyzeExpenseRequest.class))).thenReturn(response);
        when(analyzeExpenseResponseMapper.toEntity(response)).thenReturn(null);

        assertThatThrownBy(() -> {
            ReceiptExtractRequestDTO dto = new ReceiptExtractRequestDTO(1L, "test-receipt.png");
            receiptService.extract(dto);
        }).isInstanceOf(ReceiptNotExtractedException.class).hasMessage("Couldn't extract receipt data");
        
        verifyNoInteractions(receiptRepository, receiptMapper);

    }
    
    @Test
    void shouldSaveReceiptWhenSuccess() {
        User user = createMockUser();
        Receipt mappedReceipt = createMockReceipt();
        Receipt savedReceipt = createMockReceipt();
        savedReceipt.setId(1L);
        ReceiptDTO expectedDto = createMockReceiptDTO();
        AnalyzeExpenseResponse response = AnalyzeExpenseResponse.builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(textractClient.analyzeExpense(any(AnalyzeExpenseRequest.class))).thenReturn(response);
        when(analyzeExpenseResponseMapper.toEntity(response)).thenReturn(mappedReceipt);
        when(receiptRepository.save(mappedReceipt)).thenReturn(savedReceipt);
        when(receiptMapper.toDTO(savedReceipt)).thenReturn(expectedDto);
        
        ReceiptExtractRequestDTO dto = new ReceiptExtractRequestDTO(1L, "test-receipt.png");
        ReceiptDTO resultDto = receiptService.extract(dto);
        
        assertThat(resultDto).isEqualTo(expectedDto);
        assertThat(mappedReceipt.getUser()).isEqualTo(user);
        
        ArgumentCaptor<AnalyzeExpenseRequest> captor = ArgumentCaptor.forClass(AnalyzeExpenseRequest.class);
        verify(textractClient).analyzeExpense(captor.capture());
        assertThat(captor.getValue().document().s3Object().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().document().s3Object().name()).isEqualTo("test-receipt.png");
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

    private Receipt createMockReceipt() {
        Receipt receipt = new Receipt();

        receipt.setVendorName("Tesco");
        receipt.setTransactionAmount(BigDecimal.valueOf(21.99));
        receipt.setTotalAmount(BigDecimal.valueOf(22.99));
        receipt.setTransactionDate(LocalDate.now());
        receipt.setImageUrl("https://google.com");
        receipt.setOcrStatus(OCRStatus.COMPLETED);
        
        return receipt;
    }

    private ReceiptDTO createMockReceiptDTO() {
        ReceiptDTO dto = new ReceiptDTO(1L, "Tesco", BigDecimal.valueOf(21.99), BigDecimal.valueOf(22.99), LocalDate.now(), "https://google.com", OCRStatus.COMPLETED, 1L);
        return dto;
    }
}
