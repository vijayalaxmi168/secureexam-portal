package com.examportal.service;

import com.examportal.dto.*;
import com.examportal.entity.RefreshToken;
import com.examportal.entity.User;
import com.examportal.exception.DuplicateResourceException;
import com.examportal.repository.UserRepository;
import com.examportal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil             jwtUtil;
    private final UserDetailsService  userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_STUDENT)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException on failure → caught by GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        return buildAuthResponse(user);
    }

    /**
     * Refresh token rotation:
     *  1. Validate incoming refresh token
     *  2. Revoke it (so it can never be reused)
     *  3. Issue a fresh access token + new refresh token
     */
    public AuthResponse refreshTokens(RefreshRequest request) {
        RefreshToken oldToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        refreshTokenService.revokeToken(oldToken.getToken());
        return buildAuthResponse(oldToken.getUser());
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails  = userDetailsService.loadUserByUsername(user.getUsername());
        String      accessToken  = jwtUtil.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken.getToken(),
                                user.getUsername(), user.getRole().name());
    }
}
