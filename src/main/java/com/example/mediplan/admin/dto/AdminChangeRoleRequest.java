package com.example.mediplan.admin.dto;

import com.example.mediplan.user.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * When target role = MEDECIN, the fields specialty & licenseNumber are REQUIRED.
 * Others are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChangeRoleRequest {
    @NotNull(message = "Le rôle cible est obligatoire")
    private Role role;

    // Doctor-specific fields (used only when role == MEDECIN)
    private String specialty;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String clinicName;
    @Valid
    private AdminAddressInput clinicAddress; // your existing address input DTO
}
