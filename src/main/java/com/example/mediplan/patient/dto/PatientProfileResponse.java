package com.example.mediplan.patient.dto;

import com.example.mediplan.user.Address;
import com.example.mediplan.user.EmergencyContact;
import com.example.mediplan.user.Gender;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientProfileResponse {

    private final String id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final Gender gender;
    private final LocalDate dateOfBirth;
    private final Address address;
    private final String insuranceNumber;
    private final EmergencyContact emergencyContact;
    private final String avatarUrl;
}
