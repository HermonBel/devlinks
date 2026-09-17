package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.security.JwtService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setDisplayName("Test User");
        existingUser.setPasswordHash("$2a$10$hashed");
    }

    // ---- Register ----

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest req = new RegisterRequest("new@example.com", "password123", "New User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$newhash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt");

        AuthResponse result = authService.register(req);

        assertThat(result.token()).isEqualTo("test-jwt");
        assertThat(result.user().email()).isEqualTo("new@example.com");
        assertThat(result.user().displayName()).isEqualTo("New User");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldNormalizeEmailToLowercase() {
        RegisterRequest req = new RegisterRequest("MiXeD@EXAMPLE.com", "password123", "Mixed");

        when(userRepository.existsByEmail("mixed@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        authService.register(req);

        verify(userRepository).existsByEmail("mixed@example.com");
    }

    @Test
    void register_shouldThrowIfEmailTaken() {
        RegisterRequest req = new RegisterRequest("taken@example.com", "password123", "Taken");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any());
    }

    // ---- Login ----

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest req = new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashed")).thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("test-jwt");

        AuthResponse result = authService.login(req);

        assertThat(result.token()).isEqualTo("test-jwt");
        assertThat(result.user().id()).isEqualTo(1L);
    }

    @Test
    void login_shouldThrowForUnknownEmail() {
        LoginRequest req = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        LoginRequest req = new LoginRequest("test@example.com", "wrongpassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldUseSameErrorForUnknownEmailAndWrongPassword() {
        // Security: don't leak which field is wrong (prevents email enumeration)
        LoginRequest unknown = new LoginRequest("nope@example.com", "pw");
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        LoginRequest wrongPw = new LoginRequest("test@example.com", "wrong");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);

        String unknownMessage = null;
        String wrongPwMessage = null;
        try { authService.login(unknown); } catch (BadCredentialsException e) { unknownMessage = e.getMessage(); }
        try { authService.login(wrongPw); } catch (BadCredentialsException e) { wrongPwMessage = e.getMessage(); }

        assertThat(unknownMessage).isEqualTo(wrongPwMessage);
    }
}