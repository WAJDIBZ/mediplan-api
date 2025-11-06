package com.example.mediplan.user;

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

import java.time.Instant;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Document(collection = "users")
@CompoundIndexes({
        @CompoundIndex(name = "provider_providerId_idx", def = "{'provider': 1, 'providerId': 1}", unique = true, sparse = true)
})
public abstract class User {

    @Id
    private String id;

    private String fullName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private boolean emailVerified;

    private Role role;

    private String phone;

    private String avatarUrl;

    private Address address;

    private String provider;

    private String providerId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
