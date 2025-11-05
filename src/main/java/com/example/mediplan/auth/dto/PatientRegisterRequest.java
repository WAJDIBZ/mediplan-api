package com.example.mediplan.auth.dto;

import com.example.mediplan.user.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegisterRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 3, message = "Le nom complet doit contenir au moins 3 caractères")
    @Schema(example = "Claire Martin")
    private String fullName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Schema(example = "claire.martin@example.com")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Schema(example = "motdepasseFort123")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Le numéro de téléphone doit être au format international")
    @Schema(example = "+33612345678")
    private String phone;

    @Valid
    private AddressDTO address;

    @NotNull(message = "La date de naissance est obligatoire")
    @Schema(example = "1990-05-14", type = "string", format = "date")
    private LocalDate dateOfBirth;

    @NotNull(message = "Le genre est obligatoire")
    @Schema(example = "FEMALE")
    private Gender gender;

    @Schema(example = "INS-123456789")
    private String insuranceNumber;

    @Valid
    private EmergencyContactDTO emergencyContact;
}
