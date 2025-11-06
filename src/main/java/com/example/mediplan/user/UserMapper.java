package com.example.mediplan.user;

import com.example.mediplan.auth.dto.AddressDTO;
import com.example.mediplan.auth.dto.PatientRegisterRequest;
import com.example.mediplan.auth.dto.DoctorRegisterRequest;

public class UserMapper {

    public static Patient toPatientEntity(PatientRegisterRequest dto, String passwordHash) {
        return Patient.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase())
                .passwordHash(passwordHash)
                .phone(dto.getPhone())
                .avatarUrl(dto.getAvatarUrl())
                .address(map(dto.getAddress()))            // <-- OK: User.address
                .role(Role.PATIENT)
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .insuranceNumber(dto.getInsuranceNumber())
                .emergencyContact(dto.getEmergencyContact() == null ? null :
                        EmergencyContact.builder()
                                .name(dto.getEmergencyContact().getName())
                                .phone(dto.getEmergencyContact().getPhone())
                                .relation(dto.getEmergencyContact().getRelation())
                                .build())
                .build();
    }

    public static Medecin toMedecinEntity(DoctorRegisterRequest dto, String passwordHash) {
        return Medecin.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase())
                .passwordHash(passwordHash)
                .phone(dto.getPhone())
                .avatarUrl(dto.getAvatarUrl())
                .address(map(dto.getAddress()))            // <-- adresse perso (facultatif)
                .role(Role.DOCTOR)
                .specialty(dto.getSpecialty())             // <-- Champ de Medecin
                .licenseNumber(dto.getLicenseNumber())
                .yearsOfExperience(dto.getYearsOfExperience())
                .clinicName(dto.getClinicName())
                .clinicAddress(map(dto.getClinicAddress())) // <-- adresse cabinet (si envoyée)
                .build();
    }

    private static Address map(AddressDTO d) {
        if (d == null) return null;
        return Address.builder()
                .line1(d.getLine1())
                .line2(d.getLine2())
                .city(d.getCity())
                .country(d.getCountry())
                .zip(d.getZip())
                .build();
    }
}
