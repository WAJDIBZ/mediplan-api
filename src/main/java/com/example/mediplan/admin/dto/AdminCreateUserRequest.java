package com.example.mediplan.admin.dto;

import com.example.mediplan.user.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, message = "Le nom complet doit contenir au moins 3 caractères")
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Le numéro de téléphone doit être valide")
    private String phone;

    private String avatarUrl;

    @Valid
    private AdminAddressInput address;

    private String insuranceNumber;

    @Valid
    private AdminEmergencyContactInput emergencyContact;

    @Valid
    private AdminAddressInput clinicAddress;

    private String specialty;

    private String licenseNumber;

    @Min(value = 0, message = "Les années d'expérience doivent être positives")
    private Integer yearsOfExperience;

    private String clinicName;
}
