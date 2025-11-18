package com.example.mediplan.agenda.rendezvous;

import com.example.mediplan.agenda.rendezvous.dto.ChangementStatutRequest;
import com.example.mediplan.agenda.rendezvous.dto.RendezVousRequest;
import com.example.mediplan.agenda.rendezvous.dto.RendezVousResponse;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rdv")
@RequiredArgsConstructor
public class RendezVousController {

    private final RendezVousService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public RendezVousResponse creer(@Valid @RequestBody RendezVousRequest request, Authentication authentication) {
        return service.creer(request, utilisateur(authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public RendezVousResponse mettreAJour(@PathVariable String id, @Valid @RequestBody RendezVousRequest request,
            Authentication authentication) {
        return service.modifier(id, request, utilisateur(authentication));
    }



    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public RendezVousResponse changerStatut(@PathVariable String id,
            @Valid @RequestBody ChangementStatutRequest request, Authentication authentication) {
        return service.changerStatut(id, request, utilisateur(authentication));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public Page<RendezVousResponse> lister(
            @RequestParam(required = false) String medecinId,
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable,
            Authentication authentication) {
        User user = utilisateur(authentication);
        if (user.getRole() == Role.ADMIN) {
            return service.listerAdmin(Optional.ofNullable(medecinId), Optional.ofNullable(patientId), from, to, pageable);
        }
        if (user.getRole() == Role.MEDECIN) {
            String cible = medecinId != null ? medecinId : user.getId();
            if (!cible.equals(user.getId())) {
                throw new com.example.mediplan.common.exception.BusinessRuleException(
                        "Vous ne pouvez consulter que vos propres rendez-vous");
            }
            return service.listerPourMedecin(user.getId(), from, to, pageable);
        }
        String ciblePatient = patientId != null ? patientId : user.getId();
        if (!ciblePatient.equals(user.getId())) {
            throw new com.example.mediplan.common.exception.BusinessRuleException(
                    "Vous ne pouvez consulter que vos propres rendez-vous");
        }
        return service.listerPourPatient(user.getId(), from, to, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public RendezVousResponse consulter(@PathVariable String id, Authentication authentication) {
        return service.consulter(id, utilisateur(authentication));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN','PATIENT')")
    public void annuler(@PathVariable String id, Authentication authentication) {
        service.supprimer(id, utilisateur(authentication));
    }




    private User utilisateur(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
