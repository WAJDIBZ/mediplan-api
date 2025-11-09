package com.example.mediplan.agenda.disponibilite;

import com.example.mediplan.agenda.disponibilite.dto.DisponibiliteRequest;
import com.example.mediplan.agenda.disponibilite.dto.DisponibiliteResponse;
import com.example.mediplan.common.exception.BusinessRuleException;
import com.example.mediplan.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class DisponibiliteService {

    private final DisponibiliteRepository repository;

    public DisponibiliteResponse creer(@Valid DisponibiliteRequest request) {
        if (!request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new BusinessRuleException("L'heure de fin doit être postérieure à l'heure de début");
        }
        Disponibilite disponibilite = Disponibilite.builder()
                .medecinId(request.getMedecinId())
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .recurrence(request.getRecurrence())
                .commentaire(request.getCommentaire())
                .build();
        Disponibilite saved = repository.save(disponibilite);
        return toResponse(saved);
    }

    public List<DisponibiliteResponse> listerPourMedecin(@NotBlank String medecinId, LocalDate from, LocalDate to) {
        List<Disponibilite> items;
        if (from != null && to != null) {
            items = repository.findByMedecinIdAndDateBetweenAndActifIsTrue(medecinId, from, to);
        } else {
            items = repository.findByMedecinIdAndActifIsTrue(medecinId);
        }
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DisponibiliteResponse mettreAJour(@NotBlank String id, @Valid DisponibiliteRequest request, boolean admin) {
        Disponibilite disponibilite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable"));
        if (!admin && !disponibilite.getMedecinId().equals(request.getMedecinId())) {
            throw new BusinessRuleException("Vous ne pouvez modifier que vos propres disponibilités");
        }
        if (!request.getHeureFin().isAfter(request.getHeureDebut())) {
            throw new BusinessRuleException("L'heure de fin doit être postérieure à l'heure de début");
        }
        disponibilite.setDate(request.getDate());
        disponibilite.setHeureDebut(request.getHeureDebut());
        disponibilite.setHeureFin(request.getHeureFin());
        disponibilite.setRecurrence(request.getRecurrence());
        disponibilite.setCommentaire(request.getCommentaire());
        Disponibilite saved = repository.save(disponibilite);
        return toResponse(saved);
    }

    public void supprimer(@NotBlank String id, @NotBlank String medecinId, boolean admin) {
        Disponibilite disponibilite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable"));
        if (!admin && !disponibilite.getMedecinId().equals(medecinId)) {
            throw new BusinessRuleException("Vous ne pouvez supprimer que vos propres disponibilités");
        }
        repository.delete(disponibilite);
    }

    public DisponibiliteResponse basculerActivation(@NotBlank String id, boolean actif) {
        Disponibilite disponibilite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable"));
        disponibilite.setActif(actif);
        return toResponse(repository.save(disponibilite));
    }

    public Disponibilite trouver(@NotBlank String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilité introuvable"));
    }

    public List<Disponibilite> trouverPourDate(@NotBlank String medecinId, @NotNull LocalDate date) {
        return repository.findByMedecinIdAndDateAndActifIsTrue(medecinId, date);
    }

    private DisponibiliteResponse toResponse(Disponibilite disponibilite) {
        return DisponibiliteResponse.builder()
                .id(disponibilite.getId())
                .medecinId(disponibilite.getMedecinId())
                .date(disponibilite.getDate())
                .heureDebut(disponibilite.getHeureDebut())
                .heureFin(disponibilite.getHeureFin())
                .actif(disponibilite.isActif())
                .recurrence(disponibilite.getRecurrence())
                .commentaire(disponibilite.getCommentaire())
                .createdAt(disponibilite.getCreatedAt())
                .updatedAt(disponibilite.getUpdatedAt())
                .build();
    }
}
