package com.cartex.application.usecase;

import com.cartex.application.dto.UserRequestDto;
import com.cartex.application.dto.UserResponseDto;
import com.cartex.domain.model.Role;
import com.cartex.domain.model.User;
import com.cartex.domain.port.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UserResponseDto createUser(UserRequestDto request) {
        if (userRepositoryPort.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .active(true)
                .build();

        User saved = userRepositoryPort.save(user);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepositoryPort.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponseDto updateUser(Long id, UserRequestDto request) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());

        User updated = userRepositoryPort.save(user);
        return mapToResponse(updated);
    }

    public void deleteUser(Long id) {
        userRepositoryPort.deleteById(id);
    }

    private UserResponseDto mapToResponse(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
