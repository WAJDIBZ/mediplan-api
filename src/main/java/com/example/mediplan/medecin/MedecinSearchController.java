package com.example.mediplan.medecin;

import com.example.mediplan.medecin.dto.MedecinSearchRequest;
import com.example.mediplan.medecin.dto.MedecinSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medecins")
@RequiredArgsConstructor
public class MedecinSearchController {

    private final MedecinSearchService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN','MEDECIN')")
    public Page<MedecinSearchResponse> rechercher(@Valid @ModelAttribute MedecinSearchRequest request, Pageable pageable) {
        return service.rechercher(request, pageable);
    }
}
