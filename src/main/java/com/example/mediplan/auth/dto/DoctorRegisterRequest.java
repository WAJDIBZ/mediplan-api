package com.example.mediplan.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorRegisterRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, message = "Le nom complet doit contenir au moins 3 caractères")
    @Schema(example = "Dr Antoine Leroy")
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Schema(example = "antoine.leroy@example.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Schema(example = "MotDePasseSolide1")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Le numéro de téléphone doit être au format international")
    @Schema(example = "+33111222333")
    private String phone;

    @Valid
    private AddressDTO clinicAddress;

    @NotBlank(message = "La spécialité est obligatoire")
    @Schema(example = "Cardiologie")
    private String specialty;

    @NotBlank(message = "Le numéro de licence est obligatoire")
    @Schema(example = "LIC-987654")
    private String licenseNumber;

    @Min(value = 0, message = "Les années d'expérience doivent être positives")
    @Schema(example = "12")
    private int yearsOfExperience;

    @Schema(example = "Clinique du Parc")
    private String clinicName;

    @Valid
    private AddressDTO address;

    @Schema(example = "https://cdn.example.com/avatars/doctor.png")
    private String avatarUrl;
}
