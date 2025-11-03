package com.example.mediplan.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
