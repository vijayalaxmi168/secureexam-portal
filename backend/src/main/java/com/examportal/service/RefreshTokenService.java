package com.examportal.service;

import com.examportal.entity.RefreshToken;
import com.examportal.entity.User;
import com.examportal.exception.ResourceNotFoundException;
import com.examportal.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    /**
     * Token Rotation Strategy:
     * Each login/refresh deletes the old refresh token and issues a brand new one.
     * If a stolen token is replayed, it will already be deleted → request rejected.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);   // invalidate old token

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiryMs))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    public RefreshToken validateRefreshToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found or already used"));

        if (token.getIsRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please log in again.");
        }
        return token;
    }

    @Transactional
    public void revokeToken(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(t -> {
            t.setIsRevoked(true);
            refreshTokenRepository.save(t);
        });
    }
}
