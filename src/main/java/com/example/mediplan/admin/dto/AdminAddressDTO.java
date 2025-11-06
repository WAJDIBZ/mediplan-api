package com.example.mediplan.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAddressDTO {
    private String line1;
    private String line2;
    private String city;
    private String country;
    private String zip;
}
