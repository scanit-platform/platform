package com.scanit.receipt.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.scanit.receipt.model.Receipt;

import software.amazon.awssdk.services.textract.model.AnalyzeExpenseResponse;
import software.amazon.awssdk.services.textract.model.ExpenseDetection;
import software.amazon.awssdk.services.textract.model.ExpenseDocument;
import software.amazon.awssdk.services.textract.model.ExpenseField;
import software.amazon.awssdk.services.textract.model.ExpenseType;

public class AnalyzeExpenseResponseMapperTests {
    private AnalyzeExpenseResponseMapper mapper;
    
    @BeforeEach
    void setUp() {
        this.mapper = new AnalyzeExpenseResponseMapper();
    }

    @Test
    void shouldReturnNullWhenNoDocuments() {
        AnalyzeExpenseResponse response = AnalyzeExpenseResponse.builder().build();
        
        Receipt receipt = mapper.toEntity(response);

        assertThat(receipt).isNull();
    }
    
    @Test
    void shouldReturnNullTransactionDateWhenUnknownDateFormat() {
        AnalyzeExpenseResponse response = AnalyzeExpenseResponse.builder()
        .expenseDocuments(ExpenseDocument.builder()
            .summaryFields(
                ExpenseField.builder()
                .type(ExpenseType.builder().text("INVOICE_RECEIPT_DATE").build())
                .valueDetection(ExpenseDetection.builder().text("not-a-date").build())
                .build()
            )
            .build()
        )
        .build();

        Receipt receipt = mapper.toEntity(response);

        assertThat(receipt).isNotNull();
        assertThat(receipt.getTransactionDate()).isNull();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "23/07/2026",
        "23-07-2026"
    })
    void shouldReturnReceiptWhenSuccess(String date) {
        AnalyzeExpenseResponse response = AnalyzeExpenseResponse.builder()
        .expenseDocuments(ExpenseDocument.builder()
            .summaryFields(
                ExpenseField.builder()
                .type(ExpenseType.builder().text("VENDOR_NAME").build())
                .valueDetection(ExpenseDetection.builder().text("Tesco").build())
                .build(),
                ExpenseField.builder()
                .type(ExpenseType.builder().text("INVOICE_RECEIPT_DATE").build())
                .valueDetection(ExpenseDetection.builder().text(date).build())
                .build(),
                ExpenseField.builder()
                .type(ExpenseType.builder().text("SUBTOTAL").build())
                .valueDetection(ExpenseDetection.builder().text("21.99").build())
                .build(),
                ExpenseField.builder()
                .type(ExpenseType.builder().text("TOTAL").build())
                .valueDetection(ExpenseDetection.builder().text("22.99").build())
                .build()
            )
            .build()
        )
        .build();
        
        Receipt receipt = mapper.toEntity(response);

        assertThat(receipt).isNotNull();
        assertThat(receipt.getTransactionDate()).isNotNull();
    }
}
