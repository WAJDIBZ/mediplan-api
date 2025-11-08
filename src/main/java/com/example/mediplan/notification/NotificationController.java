package com.example.mediplan.notification;

import com.example.mediplan.notification.dto.NotificationPlanifieeRequest;
import com.example.mediplan.notification.dto.NotificationPlanifieeResponse;
import com.example.mediplan.notification.dto.NotificationPreferenceRequest;
import com.example.mediplan.notification.dto.NotificationPreferenceResponse;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/preferences/me")
    @PreAuthorize("hasAnyRole('PATIENT','MEDECIN','ADMIN')")
    public NotificationPreferenceResponse lirePreferences(Authentication authentication) {
        return service.getPreferences((User) authentication.getPrincipal());
    }

    @PutMapping("/preferences/me")
    @PreAuthorize("hasAnyRole('PATIENT','MEDECIN','ADMIN')")
    public NotificationPreferenceResponse mettreAJourPreferences(
            @Valid @RequestBody NotificationPreferenceRequest request, Authentication authentication) {
        return service.updatePreferences((User) authentication.getPrincipal(), request);
    }

    @PostMapping("/rappels")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public NotificationPlanifieeResponse planifier(@Valid @RequestBody NotificationPlanifieeRequest request,
            Authentication authentication) {
        return service.planifier(request, (User) authentication.getPrincipal());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<NotificationPlanifieeResponse> lister(Pageable pageable) {
        return service.lister(pageable);
    }

    @PatchMapping("/{id}/etat")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationPlanifieeResponse marquer(@PathVariable String id, @RequestBody(required = false) Boolean succes) {
        return service.marquerEnvoyee(id, succes == null || succes);
    }

    @PostMapping("/rappels/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public List<NotificationPlanifieeResponse> executer() {
        return service.declencherRappels();
    }
}
