package com.example.mediplan.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.mediplan.auth.dto.AuthResponse;
import com.example.mediplan.auth.dto.LoginRequest;
import com.example.mediplan.common.exception.InvalidCredentialsException;
import com.example.mediplan.security.jwt.JwtService;
import com.example.mediplan.user.Administrator;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.UserMapper;
import com.example.mediplan.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokensWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("Admin@Example.com ");
        request.setPassword("Secret123");

        Administrator admin = Administrator.builder()
                .id("user-1")
                .fullName("Admin Test")
                .email("admin@example.com")
                .passwordHash("HASH")
                .role(Role.ADMIN)
                .build();

        when(userService.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));
        when(userService.checkPassword("Secret123", "HASH")).thenReturn(true);
        when(jwtService.generateAccessToken("user-1", "admin@example.com", "ADMIN"))
                .thenReturn("ACCESS");
        when(jwtService.generateRefreshToken("user-1"))
                .thenReturn("REFRESH");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("ACCESS");
        assertThat(response.getRefreshToken()).isEqualTo("REFRESH");
        assertThat(response.getRole()).isEqualTo("ADMIN");

        verify(userService).findByEmail("admin@example.com");
    }

    @Test
    void loginShouldFailWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("secret");

        when(userService.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldFailWhenPasswordMissing() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("   ");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verifyNoInteractions(jwtService);
    }
}
