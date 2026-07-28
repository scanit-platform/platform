package com.scanit.receipt.mapper;

import com.scanit.receipt.dto.ReceiptDTO;
import com.scanit.receipt.model.Receipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReceiptMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "generalCategory.id", target = "generalCategoryId")
    @Mapping(source = "customCategory.id", target = "customCategoryId")
    @Mapping(source = "duplicateOf.id", target = "duplicateOfReceiptId")
    ReceiptDTO toDTO(Receipt receipt);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "generalCategory", ignore = true)
    @Mapping(target = "customCategory", ignore = true)
    @Mapping(target = "ocrStatus", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "imageHash", ignore = true)
    @Mapping(target = "lineItems", ignore = true)
    @Mapping(target = "duplicate", ignore = true)
    @Mapping(target = "savedAsDuplicate", ignore = true)
    @Mapping(target = "duplicateOf", ignore = true)
    @Mapping(target = "duplicateMatchReason", ignore = true)
    Receipt toEntity(ReceiptDTO dto);
}