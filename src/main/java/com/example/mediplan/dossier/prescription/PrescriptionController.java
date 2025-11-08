package com.example.mediplan.dossier.prescription;

import com.example.mediplan.dossier.prescription.dto.PrescriptionRequest;
import com.example.mediplan.dossier.prescription.dto.PrescriptionResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public PrescriptionResponse creer(@Valid @RequestBody PrescriptionRequest request, Authentication authentication) {
        return service.creer(request, (User) authentication.getPrincipal());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public Page<PrescriptionResponse> lister(Pageable pageable, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getRole() == Role.ADMIN) {
            return service.listerTous(pageable);
        }
        if (user.getRole() == Role.MEDECIN) {
            return service.listerPourMedecin(user.getId(), pageable);
        }
        return service.listerPourPatient(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public PrescriptionResponse consulter(@PathVariable String id, Authentication authentication) {
        return service.consulter(id, (User) authentication.getPrincipal());
    }
}
