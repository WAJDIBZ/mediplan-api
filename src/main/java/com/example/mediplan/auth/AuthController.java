package com.example.mediplan.auth;

import com.example.mediplan.auth.dto.AuthResponse;
import com.example.mediplan.auth.dto.DoctorRegisterRequest;
import com.example.mediplan.auth.dto.LoginRequest;
import com.example.mediplan.auth.dto.PatientRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    @Operation(summary = "Inscription d'un patient",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PatientRegisterRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inscription réussie",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Requête invalide"),
                    @ApiResponse(responseCode = "409", description = "Conflit de données")
            })
    public ResponseEntity<AuthResponse> registerPatient(@RequestBody @Valid PatientRegisterRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/register/doctor")
    @Operation(summary = "Inscription d'un médecin",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = DoctorRegisterRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inscription réussie",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Requête invalide"),
                    @ApiResponse(responseCode = "409", description = "Conflit de données")
            })
    public ResponseEntity<AuthResponse> registerDoctor(@RequestBody @Valid DoctorRegisterRequest request) {
        return ResponseEntity.ok(authService.registerDoctor(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Connexion réussie",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
            })
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouvellement du jeton",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Jetons régénérés",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Token invalide")
            })
    public ResponseEntity<AuthResponse> refresh(@RequestParam("token") String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @GetMapping("/me")
    public ResponseEntity<Void> me(@RequestAttribute(name = "userId", required = false) String userId) {
        return ResponseEntity.ok().build();
    }
}