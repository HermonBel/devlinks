package com.example.demo.security;

import com.example.demo.user.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 64-byte base64-encoded key for testing
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtNjQtYnl0ZXMtbG9uZy1mb3ItaG1hYy1zaGEyNTYtYWxnb3JpdGhtLXRlc3Q=";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, 3600000L); // 1 hour

        testUser = new User();
        testUser.setId(42L);
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test");
    }

    @Test
    void generateToken_shouldReturnValidJwt() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void parseToken_shouldReturnCorrectClaims() {
        String token = jwtService.generateToken(testUser);

        Claims claims = jwtService.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void extractUserId_shouldReturnTheUserId() {
        String token = jwtService.generateToken(testUser);

        Long userId = jwtService.extractUserId(token);

        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void extractEmail_shouldReturnTheEmail() {
        String token = jwtService.generateToken(testUser);

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void isValid_shouldReturnTrueForFreshToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalseForGarbage() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void isValid_shouldReturnFalseForTamperedToken() {
        String token = jwtService.generateToken(testUser);
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalseForExpiredToken() {
        JwtService shortLivedService = new JwtService(TEST_SECRET, -1000L); // already expired

        String token = shortLivedService.generateToken(testUser);

        assertThat(shortLivedService.isValid(token)).isFalse();
    }

    @Test
    void parseToken_shouldThrowForWrongSecret() {
        String token = jwtService.generateToken(testUser);
        JwtService otherService = new JwtService(
                "YW5vdGhlci1zZWNyZXQta2V5LXRoYXQtaXMtNjQtYnl0ZXMtbG9uZy1mb3ItaG1hYy1zaGEyNTYtdGVzdA==",
                3600000L
        );

        assertThatThrownBy(() -> otherService.parseToken(token))
                .isInstanceOf(Exception.class);
    }
}