package com.example.mediplan.auth;

import com.example.mediplan.auth.dto.AuthResponse;
import com.example.mediplan.auth.dto.LoginRequest;
import com.example.mediplan.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        try {
            AuthResponse response = authService.register(req);
            System.out.println("Registration successful, returning tokens");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.out.println(" Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        try {
            AuthResponse response = authService.login(req);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam("token") String refreshToken) {
        try {
            return ResponseEntity.ok(authService.refresh(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Token invalide"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestAttribute(name = "userId", required = false) String userId) {
        return ResponseEntity.ok().build();
    }
}