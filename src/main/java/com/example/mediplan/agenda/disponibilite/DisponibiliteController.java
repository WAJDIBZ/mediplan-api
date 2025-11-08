package com.example.mediplan.agenda.disponibilite;

import com.example.mediplan.agenda.disponibilite.dto.DisponibiliteRequest;
import com.example.mediplan.agenda.disponibilite.dto.DisponibiliteResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DisponibiliteController {

    private final DisponibiliteService service;

    @PostMapping("/medecins/{medecinId}/disponibilites")
    @PreAuthorize("hasAnyRole('MEDECIN','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibiliteResponse creer(@PathVariable String medecinId,
            @Valid @RequestBody DisponibiliteRequest request,
            Authentication authentication) {
        request.setMedecinId(medecinId);
        verifierAcces(authentication, medecinId);
        return service.creer(request);
    }

    @GetMapping("/medecins/{medecinId}/disponibilites")
    @PreAuthorize("hasAnyRole('MEDECIN','ADMIN','PATIENT')")
    public List<DisponibiliteResponse> lister(@PathVariable String medecinId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.listerPourMedecin(medecinId, from, to);
    }

    @PutMapping("/medecins/{medecinId}/disponibilites/{id}")
    @PreAuthorize("hasAnyRole('MEDECIN','ADMIN')")
    public DisponibiliteResponse mettreAJour(@PathVariable String medecinId, @PathVariable String id,
            @Valid @RequestBody DisponibiliteRequest request, Authentication authentication) {
        request.setMedecinId(medecinId);
        boolean admin = verifierAcces(authentication, medecinId);
        return service.mettreAJour(id, request, admin);
    }

    @DeleteMapping("/medecins/{medecinId}/disponibilites/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('MEDECIN','ADMIN')")
    public void supprimer(@PathVariable String medecinId, @PathVariable String id, Authentication authentication) {
        boolean admin = verifierAcces(authentication, medecinId);
        service.supprimer(id, medecinId, admin);
    }

    private boolean verifierAcces(Authentication authentication, String medecinId) {
        User user = (User) authentication.getPrincipal();
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        if (user.getRole() == Role.MEDECIN && !user.getId().equals(medecinId)) {
            throw new com.example.mediplan.common.exception.BusinessRuleException(
                    "Vous ne pouvez gérer que vos propres disponibilités");
        }
        return false;
    }
}
