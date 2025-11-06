// src/main/java/com/example/mediplan/user/UserMapper.java
package com.example.mediplan.user;

import org.springframework.stereotype.Component;

import com.example.mediplan.auth.dto.PatientRegisterRequest;
import com.example.mediplan.auth.dto.DoctorRegisterRequest;
import com.example.mediplan.auth.dto.AddressDTO;
import com.example.mediplan.auth.dto.EmergencyContactDTO;

@Component
public class UserMapper {

    // -------- Patient --------
    public Patient toPatient(PatientRegisterRequest dto, String passwordHash, String emailLower) {
        return Patient.builder()
                .fullName(trim(dto.getFullName()))
                .email(emailLower)                 // <- on utilise le param déjà en lower
                .passwordHash(passwordHash)
                .phone(trimNullable(dto.getPhone()))
                .avatarUrl(trimNullable(dto.getAvatarUrl()))
                .address(map(dto.getAddress()))   // <- adresse perso (optionnelle)
                .role(Role.PATIENT)
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .insuranceNumber(trimNullable(dto.getInsuranceNumber()))
                .emergencyContact(map(dto.getEmergencyContact()))
                .build();
    }

    // -------- Médecin --------
    public Medecin toMedecin(DoctorRegisterRequest dto, String passwordHash, String emailLower) {
        return Medecin.builder()
                .fullName(trim(dto.getFullName()))
                .email(emailLower)
                .passwordHash(passwordHash)
                .phone(trimNullable(dto.getPhone()))
                .avatarUrl(trimNullable(dto.getAvatarUrl()))
                .address(map(dto.getAddress()))          // <- adresse perso (si tu l’envoies)
                .role(Role.DOCTOR)
                .specialty(trim(dto.getSpecialty()))
                .licenseNumber(trim(dto.getLicenseNumber()))
                .yearsOfExperience(dto.getYearsOfExperience())
                .clinicName(trimNullable(dto.getClinicName()))
                .clinicAddress(map(dto.getClinicAddress()))  // <- adresse du cabinet (optionnelle)
                .build();
    }

    // -------- Helpers mapping --------
    private Address map(AddressDTO d) {
        if (d == null) return null;
        return Address.builder()
                .line1(trim(d.getLine1()))
                .line2(trimNullable(d.getLine2()))
                .city(trim(d.getCity()))
                .country(trim(d.getCountry()))
                .zip(trim(d.getZip()))
                .build();
    }

    private EmergencyContact map(EmergencyContactDTO ec) {
        if (ec == null) return null;
        return EmergencyContact.builder()
                .name(trim(ec.getName()))
                .phone(trim(ec.getPhone()))
                .relation(trim(ec.getRelation()))
                .build();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String trimNullable(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }
}
