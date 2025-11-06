package com.example.mediplan.admin.dto;

import com.example.mediplan.user.Gender;
import com.example.mediplan.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsDTO {
    private String id;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    private boolean emailVerified;
    private String phone;
    private String avatarUrl;
    private String provider;
    private String providerId;
    private Instant createdAt;
    private Instant updatedAt;

    private LocalDate dateOfBirth;
    private Gender gender;
    private String insuranceNumber;
    private AdminEmergencyContactDTO emergencyContact;
    private AdminAddressDTO address;

    private String specialty;
    private String licenseNumber;
    private Integer yearsOfExperience;
    private String clinicName;
    private AdminAddressDTO clinicAddress;
}
