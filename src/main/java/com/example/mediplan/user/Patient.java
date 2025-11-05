package com.example.mediplan.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "users")
@TypeAlias("patient")
public class Patient extends User {

    @Field(targetType = FieldType.STRING)
    private LocalDate dateOfBirth;

    private Gender gender;

    private String insuranceNumber;

    private EmergencyContact emergencyContact;
}
