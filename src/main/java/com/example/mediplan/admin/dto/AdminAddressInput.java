package com.example.mediplan.admin.dto;

import lombok.Data;

@Data
public class AdminAddressInput {
    private String line1;
    private String line2;
    private String city;
    private String country;
    private String zip;
}
