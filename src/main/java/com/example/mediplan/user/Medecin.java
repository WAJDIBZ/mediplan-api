package com.example.mediplan.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@TypeAlias("medecin")
@Getter @Setter

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Medecin extends User {
    // no duplicate fields that already exist in User
    // DO NOT write any manual constructors
}
