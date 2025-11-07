// src/main/java/com/example/mediplan/user/Administrator.java
package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@TypeAlias("admin")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Administrator extends User {
    // No extra fields unless truly admin-only (and not in User).
    // Remove any @Override methods that don't match signatures in User.
}
