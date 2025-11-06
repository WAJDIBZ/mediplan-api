package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@TypeAlias("doctor")
@Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder
public class Medecin extends User {

    private String specialty;

    @Indexed(unique = true, sparse = true)
    private String licenseNumber;

    private int yearsOfExperience;
    private String clinicName;

    // Si tu veux distinguer l'adresse du cabinet de l'adresse perso du User :
    private Address clinicAddress;
}
