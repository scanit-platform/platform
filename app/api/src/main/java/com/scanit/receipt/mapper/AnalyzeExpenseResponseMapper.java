package com.scanit.receipt.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.scanit.receipt.model.OCRStatus;
import com.scanit.receipt.model.Receipt;

import software.amazon.awssdk.services.textract.model.AnalyzeExpenseResponse;
import software.amazon.awssdk.services.textract.model.ExpenseDocument;
import software.amazon.awssdk.services.textract.model.ExpenseField;

@Component
public class AnalyzeExpenseResponseMapper {
    public Receipt toEntity(AnalyzeExpenseResponse response) {
        if (response.expenseDocuments().isEmpty()) {
            return null;
        }
        
        Receipt receipt = new Receipt();

        ExpenseDocument doc = response.expenseDocuments().getFirst();

        for (ExpenseField field: doc.summaryFields()) {
            String type = field.type().text();
            String value = field.valueDetection() != null ? field.valueDetection().text().trim() : null;
            
            switch(type) {
                case "VENDOR_NAME" -> receipt.setVendorName(value);
                case "INVOICE_RECEIPT_DATE" -> receipt.setTransactionDate(LocalDate.parse(value));
                case "SUBTOTAL" -> {
                    BigDecimal amount = new BigDecimal(value);
                    receipt.setTransactionAmount(amount);
                }
                case "TOTAL" -> {
                    BigDecimal amount = new BigDecimal(value);
                    receipt.setTotalAmount(amount);
                }
            }
        }

        receipt.setOcrStatus(OCRStatus.COMPLETED);

        return receipt;
    }
}
