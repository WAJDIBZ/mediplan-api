package com.example.mediplan.user;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Document(collection = "users")
@CompoundIndexes({
        @CompoundIndex(name = "provider_providerId_idx", def = "{'provider': 1, 'providerId': 1}", unique = true, sparse = true)
})
public class User {

    @Id
    private String id;

    @Indexed
    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private boolean emailVerified;

    private Role role;

    @Indexed
    @Builder.Default
    private boolean active = true;

    private String phone;

    private String avatarUrl;

    private Address address;

    @Field(targetType = FieldType.STRING)
    private LocalDate dateOfBirth;

    private Gender gender;

    private String insuranceNumber;

    private EmergencyContact emergencyContact;

    @Indexed
    private String specialty;

    @Indexed(unique = true, sparse = true)
    private String licenseNumber;

    private Integer yearsOfExperience;

    private String clinicName;

    private Address clinicAddress;

    private String provider;

    private String providerId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
