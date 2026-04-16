package com.erp.auth.service;

import com.erp.admin.entity.User;
import com.erp.admin.repository.UserRepository;
import com.erp.auth.dto.*;
import com.erp.auth.security.JwtTokenProvider;
import com.erp.auth.security.UserPrincipal;
import com.erp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailWithRole(request.getEmail())
                .orElseThrow(() -> new BusinessException("AUTH_001", "Invalid email or password"));

        if (!user.getIsActive()) {
            throw new BusinessException("AUTH_002", "Account is inactive");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getFullName())
                        .role(roleName)
                        .build())
                .build();
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException("AUTH_003", "Invalid refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findByIdWithRole(userId)
                .orElseThrow(() -> new BusinessException("AUTH_004", "User not found"));

        String accessToken = tokenProvider.generateAccessToken(userId, user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : "USER");

        return TokenResponse.builder()
                .accessToken(accessToken)
                .expiresIn(900)
                .build();
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
