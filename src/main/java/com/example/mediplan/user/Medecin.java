// src/main/java/com/example/mediplan/user/Medecin.java
package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@TypeAlias("medecin")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Medecin extends User {
    // No duplicate fields:
    // specialty, licenseNumber, yearsOfExperience, clinicName, clinicAddress
    // are already in User — just inherit them.
}
