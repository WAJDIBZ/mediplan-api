package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@TypeAlias("patient")
@Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder
public class Patient extends User {

    private LocalDate dateOfBirth;



}
