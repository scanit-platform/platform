package com.scanit.user.service;

import com.scanit.user.dto.UserRequestDto;
import com.scanit.user.dto.UserResponseDto;

public interface UserService {
    UserResponseDto registerUser(UserRequestDto user);
}
