package com.example.mediplan.patient;

import com.example.mediplan.patient.dto.PatientProfileResponse;
import com.example.mediplan.patient.dto.PatientProfileUpdateRequest;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class PatientProfileService {

    private final UserRepository userRepository;

    public PatientProfileResponse getProfile(User user) {
        if (!(user instanceof Patient patient)) {
            throw new IllegalStateException("Profil patient indisponible pour ce rôle");
        }
        return toResponse(patient);
    }

    public PatientProfileResponse updateProfile(Patient patient, @Valid PatientProfileUpdateRequest request) {
        patient.setFullName(request.getFullName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setInsuranceNumber(request.getInsuranceNumber());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setAvatarUrl(request.getAvatarUrl());
        Patient saved = (Patient) userRepository.save(patient);
        return toResponse(saved);
    }

    private PatientProfileResponse toResponse(Patient patient) {
        return PatientProfileResponse.builder()
                .id(patient.getId())
                .fullName(patient.getFullName())
                .email(patient.getEmail())
                .phone(patient.getPhone())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth())
                .address(patient.getAddress())
                .insuranceNumber(patient.getInsuranceNumber())
                .emergencyContact(patient.getEmergencyContact())
                .avatarUrl(patient.getAvatarUrl())
                .build();
    }
}
