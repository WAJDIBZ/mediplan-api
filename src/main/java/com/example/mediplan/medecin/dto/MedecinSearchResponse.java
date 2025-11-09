package com.example.mediplan.medecin.dto;

import com.example.mediplan.user.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedecinSearchResponse {

    private final String id;
    private final String fullName;
    private final String email;
    private final String specialty;
    private final String phone;
    private final Address clinicAddress;
    private final Integer yearsOfExperience;
    private final boolean active;
}
