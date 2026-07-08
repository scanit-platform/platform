package com.scanit.receipt.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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
                case "INVOICE_RECEIPT_DATE" -> receipt.setTransactionDate(parseTransactionDate(value));
                case "SUBTOTAL" -> receipt.setTransactionAmount(parseAmount(value));
                case "TOTAL" -> receipt.setTotalAmount(parseAmount(value));
            }
        }

        receipt.setOcrStatus(OCRStatus.COMPLETED);

        return receipt;
    }
    
    private BigDecimal parseAmount(String value) {
        try {
            value = value.replace("$", "").replace("€", "").replace("£", "");
            BigDecimal amount = new BigDecimal(value);
            return amount;
        } catch(NumberFormatException e) {
            return new BigDecimal(0);
        }
    }

    private LocalDate parseTransactionDate(String value) {
        DateTimeFormatter formats = new DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        .toFormatter();

        LocalDate date = LocalDate.parse(value, formats);
        return date;
    }
}
