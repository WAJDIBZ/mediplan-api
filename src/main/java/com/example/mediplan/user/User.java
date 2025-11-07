// User.java
package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    private String id;
    private String fullName;
    private String email;
    private String passwordHash;

    // 🟢 Use enum for gender (not String)
    private Gender gender;           // <— CHANGE: from String -> Gender

    // 🟢 Use wrapper type so it can be null for non-doctors
    private Integer yearsOfExperience; // <— keep as Integer everywhere

    private String phone;
    private String avatarUrl;

    private Address address;
    private String insuranceNumber;
    private EmergencyContact emergencyContact;

    private String specialty;
    private String licenseNumber;
    private String clinicName;
    private Address clinicAddress;

    private boolean emailVerified;

    // Lombok note: default with builders must use @Builder.Default
    @Builder.Default
    private boolean active = true;
    // <— fixes the @SuperBuilder warning

    private Role role;

    private String provider;   // "LOCAL", "GOOGLE", "FACEBOOK"
    private String providerId;

    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;


}
