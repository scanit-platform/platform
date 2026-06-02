package com.scanit.user.service;

import com.scanit.exception.UserAlreadyExistsException;
import com.scanit.user.dto.UserRequestDto;
import com.scanit.user.dto.UserResponseDto;
import com.scanit.user.mapper.UserMapper;
import com.scanit.user.model.User;
import com.scanit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void shouldRegisterUserAndEncodePassword() {
        UserRequestDto request = userRequestDto("Alice", "alice@example.com", "password123");
        User savedUser = new User(1L, "Alice", "alice@example.com", "encoded-password", LocalDateTime.now());
        UserResponseDto response = new UserResponseDto(1L, "Alice", "alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(savedUser)).thenReturn(response);

        UserResponseDto actualResponse = userService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persistedUser = userCaptor.getValue();

        assertThat(actualResponse).isEqualTo(response);
        assertThat(persistedUser.getId()).isNull();
        assertThat(persistedUser.getName()).isEqualTo("Alice");
        assertThat(persistedUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(persistedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(persistedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenRegisteringUserWithDuplicateEmail() {
        UserRequestDto request = userRequestDto("Alice", "alice@example.com", "password123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email alice@example.com already exists");

        verifyNoInteractions(passwordEncoder, userMapper);
    }

    private UserRequestDto userRequestDto(String name, String email, String password) {
        UserRequestDto dto = new UserRequestDto();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }
}
