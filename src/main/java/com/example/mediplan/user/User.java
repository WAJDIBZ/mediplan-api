package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("users")
@Data @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private boolean emailVerified;

    private String phone;
    private String avatarUrl;
    private String provider;     // "GOOGLE" | "FACEBOOK" | "LOCAL"
    private String providerId;   // sub/id from provider

    // <— AJOUTÉ : le mapper attend .address(...)
    private Address address;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Role role; // enum top-level: com.example.mediplan.user.Role
}
