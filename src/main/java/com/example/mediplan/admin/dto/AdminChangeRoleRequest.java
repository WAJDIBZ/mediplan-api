package com.example.mediplan.admin.dto;

import com.example.mediplan.user.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * When target role = MEDECIN, the fields specialty & licenseNumber are REQUIRED.
 * Others are optional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChangeRoleRequest {
    private Role role;

    // Doctor-specific fields (used only when role == MEDECIN)
    private String specialty;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String clinicName;
    private AdminAddressInput clinicAddress; // your existing address input DTO
}
