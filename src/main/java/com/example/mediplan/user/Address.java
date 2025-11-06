package com.example.mediplan.user;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    private String line1;
    private String line2;
    private String city;
    private String country;
    private String zip;
}
