package com.example.mediplan.user;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@TypeAlias("admin")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Document(collection = "user")
public class Administrator extends User {
    // no extra fields here unless truly admin-only
    // DO NOT write any manual constructors
}
