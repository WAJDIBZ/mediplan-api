package com.example.mediplan.agenda.rendezvous;

import com.example.mediplan.agenda.disponibilite.Disponibilite;
import com.example.mediplan.agenda.disponibilite.DisponibiliteService;
import com.example.mediplan.agenda.rendezvous.dto.ChangementStatutRequest;
import com.example.mediplan.agenda.rendezvous.dto.RendezVousRequest;
import com.example.mediplan.agenda.rendezvous.dto.RendezVousResponse;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceNotFoundException;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class RendezVousService {

    private static final Set<RendezVousStatut> STATUTS_ACTIFS = EnumSet.of(
            RendezVousStatut.PLANIFIE,
            RendezVousStatut.CONFIRME
    );
    private static final List<RendezVousStatut> STATUTS_ACTIFS_LIST = List.copyOf(STATUTS_ACTIFS);

    private final RendezVousRepository repository;
    private final DisponibiliteService disponibiliteService;

    public RendezVousResponse creer(@Valid RendezVousRequest request, User createur) {
        if (!request.getFin().isAfter(request.getDebut())) {
            throw new BusinessRuleException("La date de fin doit être postérieure à la date de début");
        }

        verifierDisponibilite(request.getMedecinId(), request.getDebut(), request.getFin());
        verifierAbsenceConflit(request.getMedecinId(), request.getDebut(), request.getFin(), null);

        RendezVous rendezVous = RendezVous.builder()
                .medecinId(request.getMedecinId())
                .patientId(request.getPatientId())
                .debut(request.getDebut())
                .fin(request.getFin())
                .motif(request.getMotif())
                .notesPrivees(request.getNotesPrivees())
                .statut(RendezVousStatut.PLANIFIE)
                .createurId(createur.getId())
                .build();
        return toResponse(repository.save(rendezVous));
    }

    public RendezVousResponse modifier(@NotBlank String id, @Valid RendezVousRequest request, User acteur) {
        RendezVous rendezVous = trouverEtVerifierAcces(id, acteur);
        if (!request.getFin().isAfter(request.getDebut())) {
            throw new BusinessRuleException("La date de fin doit être postérieure à la date de début");
        }
        verifierDisponibilite(request.getMedecinId(), request.getDebut(), request.getFin());
        verifierAbsenceConflit(request.getMedecinId(), request.getDebut(), request.getFin(), rendezVous.getId());
        rendezVous.setMedecinId(request.getMedecinId());
        rendezVous.setPatientId(request.getPatientId());
        rendezVous.setDebut(request.getDebut());
        rendezVous.setFin(request.getFin());
        rendezVous.setMotif(request.getMotif());
        rendezVous.setNotesPrivees(request.getNotesPrivees());
        return toResponse(repository.save(rendezVous));
    }

    public RendezVousResponse changerStatut(@NotBlank String id, @Valid ChangementStatutRequest request, User acteur) {
        RendezVous rendezVous = trouverEtVerifierAcces(id, acteur);
        if (rendezVous.getStatut() == RendezVousStatut.ANNULE && request.getStatut() != RendezVousStatut.ANNULE) {
            throw new BusinessRuleException("Un rendez-vous annulé ne peut pas être réouvert");
        }
        rendezVous.setStatut(request.getStatut());
        if (request.getCommentaire() != null) {
            rendezVous.setNotesPrivees(request.getCommentaire());
        }
        return toResponse(repository.save(rendezVous));
    }

    public Page<RendezVousResponse> listerPourMedecin(@NotBlank String medecinId, Instant from, Instant to, Pageable pageable) {
        Page<RendezVous> page;
        if (from != null && to != null) {
            page = repository.findByMedecinIdAndDebutBetween(medecinId, from, to, pageable);
        } else {
            page = repository.findByMedecinId(medecinId, pageable);
        }
        return page.map(this::toResponse);
    }

    public Page<RendezVousResponse> listerPourPatient(@NotBlank String patientId, Instant from, Instant to, Pageable pageable) {
        Page<RendezVous> page;
        if (from != null && to != null) {
            page = repository.findByPatientIdAndDebutBetween(patientId, from, to, pageable);
        } else {
            page = repository.findByPatientId(patientId, pageable);
        }
        return page.map(this::toResponse);
    }

    public Page<RendezVousResponse> listerAdmin(Optional<String> medecinId, Optional<String> patientId, Instant from,
            Instant to, Pageable pageable) {
        if (medecinId.isPresent()) {
            return listerPourMedecin(medecinId.get(), from, to, pageable);
        }
        if (patientId.isPresent()) {
            return listerPourPatient(patientId.get(), from, to, pageable);
        }
        if (from != null && to != null) {
            return repository.findByDebutBetween(from, to, pageable).map(this::toResponse);
        }
        return repository.findAll(pageable).map(this::toResponse);
    }

    public RendezVousResponse consulter(@NotBlank String id, User acteur) {
        RendezVous rendezVous = trouverEtVerifierAcces(id, acteur);
        return toResponse(rendezVous);
    }

    public void supprimer(@NotBlank String id, User acteur) {
        RendezVous rendezVous = trouverEtVerifierAcces(id, acteur);
        rendezVous.setStatut(RendezVousStatut.ANNULE);
        repository.save(rendezVous);
    }

    public List<RendezVous> rechercherParStatutEtPeriode(@NotNull RendezVousStatut statut, Instant from, Instant to) {
        return repository.findByStatutAndDebutBetween(statut, from, to);
    }

    public List<RendezVous> rechercherParMedecinEtStatut(@NotBlank String medecinId, RendezVousStatut statut) {
        return repository.findByMedecinIdAndStatut(medecinId, statut);
    }

    public long compterParStatut(RendezVousStatut statut) {
        return repository.countByStatut(statut);
    }

    private RendezVous trouverEtVerifierAcces(String id, User acteur) {
        RendezVous rendezVous = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable"));
        if (acteur.getRole() == Role.ADMIN) {
            return rendezVous;
        }
        if (acteur.getRole() == Role.MEDECIN && !acteur.getId().equals(rendezVous.getMedecinId())) {
            throw new BusinessRuleException("Accès refusé au rendez-vous demandé");
        }
        if (acteur.getRole() == Role.PATIENT && !acteur.getId().equals(rendezVous.getPatientId())) {
            throw new BusinessRuleException("Accès refusé au rendez-vous demandé");
        }
        return rendezVous;
    }

    private void verifierDisponibilite(String medecinId, Instant debut, Instant fin) {
        LocalDate date = LocalDateTime.ofInstant(debut, ZoneId.systemDefault()).toLocalDate();
        List<Disponibilite> disponibilites = disponibiliteService.trouverPourDate(medecinId, date);
        if (disponibilites.isEmpty()) {
            throw new BusinessRuleException("Aucune disponibilité ouverte pour ce créneau");
        }
        LocalDateTime startLocal = LocalDateTime.ofInstant(debut, ZoneId.systemDefault());
        LocalDateTime endLocal = LocalDateTime.ofInstant(fin, ZoneId.systemDefault());
        boolean couvre = disponibilites.stream().anyMatch(dispo ->
                !startLocal.toLocalTime().isBefore(dispo.getHeureDebut())
                        && !endLocal.toLocalTime().isAfter(dispo.getHeureFin()));
        if (!couvre) {
            throw new BusinessRuleException("Le créneau demandé dépasse les disponibilités publiées");
        }
    }

    private void verifierAbsenceConflit(String medecinId, Instant debut, Instant fin, String excludeId) {
        List<RendezVous> rdvActifs = repository.findByMedecinIdAndStatutIn(medecinId, STATUTS_ACTIFS_LIST);
        boolean conflit = rdvActifs.stream().anyMatch(rdv -> {
            if (excludeId != null && excludeId.equals(rdv.getId())) {
                return false;
            }
            boolean chevauche = !rdv.getFin().isBefore(debut) && !rdv.getDebut().isAfter(fin);
            return chevauche;
        });
        if (conflit) {
            throw new BusinessRuleException("Le créneau est déjà occupé pour ce médecin");
        }
    }

    private RendezVousResponse toResponse(RendezVous rendezVous) {
        return RendezVousResponse.builder()
                .id(rendezVous.getId())
                .medecinId(rendezVous.getMedecinId())
                .patientId(rendezVous.getPatientId())
                .debut(rendezVous.getDebut())
                .fin(rendezVous.getFin())
                .statut(rendezVous.getStatut())
                .motif(rendezVous.getMotif())
                .notesPrivees(rendezVous.getNotesPrivees())
                .createurId(rendezVous.getCreateurId())
                .createdAt(rendezVous.getCreatedAt())
                .updatedAt(rendezVous.getUpdatedAt())
                .build();
    }
}
