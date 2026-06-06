package com.scanit.receipt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.scanit.receipt.model.Receipt;
import com.scanit.receipt.dto.ReceiptDTO;

@Mapper(componentModel = "spring")
public interface ReceiptMapper {
    @Mapping(source = "user.id", target = "userId")
    ReceiptDTO toDTO(Receipt receipt);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ocrStatus", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "lineItems", ignore = true)
    Receipt toEntity(ReceiptDTO dto);
}
