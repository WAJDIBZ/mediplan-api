package com.example.mediplan.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TypeAlias;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
@TypeAlias("doctor")
public class Medecin extends User {

    @Indexed
    private String specialty;

    @Indexed(unique = true)
    private String licenseNumber;

    private int yearsOfExperience;

    private String clinicName;

    private Address clinicAddress;
}
