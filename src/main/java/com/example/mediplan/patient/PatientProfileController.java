package com.example.mediplan.patient;

import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.patient.dto.PatientProfileResponse;
import com.example.mediplan.patient.dto.PatientProfileUpdateRequest;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PATIENT', 'MEDECIN')")

public class PatientProfileController {

    private final PatientProfileService service;

    @GetMapping
    public PatientProfileResponse me(Authentication authentication) {
        return service.getProfile((User) authentication.getPrincipal());
    }

    @PutMapping
    public PatientProfileResponse update(@Valid @RequestBody PatientProfileUpdateRequest request,
                                         Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        if (user.getRole() == Role.PATIENT) {
            return service.updateProfile((Patient) user, request);
        }


        if (user.getRole() == Role.MEDECIN) {
            throw new BusinessRuleException("Le médecin peut consulter mais ne peut pas modifier le profil du patient.");
        }

        throw new BusinessRuleException("Accès non autorisé.");
    }

}
