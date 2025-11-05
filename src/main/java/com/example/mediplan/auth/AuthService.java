package com.example.mediplan.auth;

import com.example.mediplan.auth.dto.AuthResponse;
import com.example.mediplan.auth.dto.LoginRequest;
import com.example.mediplan.auth.dto.RegisterRequest;
import com.example.mediplan.security.jwt.JwtService;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserRepository;
import com.example.mediplan.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service

public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    public AuthService(UserService userService, UserRepository userRepository, JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        User.Role role = null;
        try { if (req.getRole()!=null) role = User.Role.valueOf(req.getRole().toUpperCase()); } catch (Exception ignored) {}
        User user = userService.createUser(req.getFullName(), req.getEmail(), req.getPassword(), role);
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getId());
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!userService.checkPassword(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getId());
        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(String refreshToken) {
        var claims = jwtService.parseRefreshToken(refreshToken).getBody();
        String userId = claims.getSubject();
        User user = userRepository.findById(userId).orElseThrow();
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefresh = jwtService.generateRefreshToken(user.getId());
        return new AuthResponse(access, newRefresh);
    }
}
