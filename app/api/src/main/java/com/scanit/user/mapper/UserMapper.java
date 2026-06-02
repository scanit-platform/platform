package com.scanit.user.mapper;

import com.scanit.user.dto.UserResponseDto;
import com.scanit.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
}
