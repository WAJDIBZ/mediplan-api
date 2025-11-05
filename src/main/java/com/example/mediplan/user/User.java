package com.example.mediplan.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor

public class User {

    @Id
    private String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean emailVerified;

    public enum Role {
        ADMIN,
        DOCTOR,
        PATIENT
    }

    public User(String id, String fullName, String email, String passwordHash, Role role, boolean emailVerified) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = emailVerified;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}