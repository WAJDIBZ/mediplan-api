package com.example.mediplan.user;

import com.example.mediplan.auth.dto.AddressDTO;
import com.example.mediplan.auth.dto.DoctorRegisterRequest;
import com.example.mediplan.auth.dto.EmergencyContactDTO;
import com.example.mediplan.auth.dto.PatientRegisterRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public Patient toPatient(PatientRegisterRequest request, String hashedPassword, String normalizedEmail) {
        return Patient.builder()
                .fullName(request.getFullName())
                .email(normalizedEmail)
                .passwordHash(hashedPassword)
                .role(Role.PATIENT)
                .emailVerified(false)
                .phone(request.getPhone())
                .address(toAddress(request.getAddress()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .insuranceNumber(request.getInsuranceNumber())
                .emergencyContact(toEmergencyContact(request.getEmergencyContact()))
                .build();
    }

    public Medecin toMedecin(DoctorRegisterRequest request, String hashedPassword, String normalizedEmail) {
        return Medecin.builder()
                .fullName(request.getFullName())
                .email(normalizedEmail)
                .passwordHash(hashedPassword)
                .role(Role.DOCTOR)
                .emailVerified(false)
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .specialty(request.getSpecialty())
                .licenseNumber(request.getLicenseNumber())
                .yearsOfExperience(request.getYearsOfExperience())
                .clinicName(request.getClinicName())
                .clinicAddress(toAddress(request.getClinicAddress()))
                .build();
    }

    private Address toAddress(AddressDTO dto) {
        if (dto == null) {
            return null;
        }
        return Address.builder()
                .line1(dto.getLine1())
                .line2(dto.getLine2())
                .city(dto.getCity())
                .country(dto.getCountry())
                .zip(dto.getZip())
                .build();
    }

    private EmergencyContact toEmergencyContact(EmergencyContactDTO dto) {
        if (dto == null) {
            return null;
        }
        return EmergencyContact.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .relation(dto.getRelation())
                .build();
    }
}
