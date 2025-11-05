package com.example.mediplan.user;

import lombok.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("users")
@TypeAlias("patient")   // <- BON package : org.springframework.data.annotation.TypeAlias
@Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true)
public class Patient extends User {

    private LocalDate dateOfBirth;
    private Gender gender;
    private String insuranceNumber;

    public enum Gender { MALE, FEMALE, OTHER, UNDISCLOSED }
}
