package com.example.mediplan.user;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    @Id
    private String id;
    private String fullName;
    private String email;
    private String passwordHash;

    private Gender gender;
    private Integer yearsOfExperience;

    private String phone;
    private String avatarUrl;

    private Address address;
    private String insuranceNumber;
    private EmergencyContact emergencyContact;

    private String specialty;
    private String licenseNumber;
    private String clinicName;
    private Address clinicAddress;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean active = true;

    private Role role;

    private String provider;   // "LOCAL", "GOOGLE", "FACEBOOK"
    private String providerId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
