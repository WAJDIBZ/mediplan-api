package com.example.mediplan.patient.dto;

import com.example.mediplan.user.Address;
import com.example.mediplan.user.EmergencyContact;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientProfileUpdateRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(max = 120, message = "Le nom complet est trop long")
    private String fullName;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @Size(max = 40, message = "Le téléphone est trop long")
    private String phone;

    @Valid
    private Address address;

    @Size(max = 60, message = "Le numéro d'assurance est trop long")
    private String insuranceNumber;

    @Valid
    private EmergencyContact emergencyContact;

    private String avatarUrl;
}
