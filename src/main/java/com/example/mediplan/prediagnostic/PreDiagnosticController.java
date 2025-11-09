package com.example.mediplan.prediagnostic;

import com.example.mediplan.prediagnostic.dto.PreDiagnosticRequest;
import com.example.mediplan.prediagnostic.dto.PreDiagnosticResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prediagnostic")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PATIENT','MEDECIN','ADMIN')")
public class PreDiagnosticController {

    private final PreDiagnosticService service;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public PreDiagnosticResponse analyser(@Valid @RequestBody PreDiagnosticRequest request) {
        return service.analyser(request);
    }
}
