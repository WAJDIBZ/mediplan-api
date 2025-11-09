package com.example.mediplan.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@TypeAlias("medecin")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Document(collection = "user")
@NoArgsConstructor
public class Medecin extends User {
    // no duplicate fields that already exist in User
    // DO NOT write any manual constructors
}
