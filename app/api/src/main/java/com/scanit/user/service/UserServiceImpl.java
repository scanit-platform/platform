package com.scanit.user.service;

import com.scanit.exception.UserAlreadyExistsException;
import com.scanit.user.dto.UserRequestDto;
import com.scanit.user.dto.UserResponseDto;
import com.scanit.user.mapper.UserMapper;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRequestDto user) {
        validateEmailUniqueness(user.getEmail());

        User newUser = new User(
                null,
                user.getName(),
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                LocalDateTime.now());
        return userMapper.toUserResponseDto(userRepository.save(newUser));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }
    }
}
