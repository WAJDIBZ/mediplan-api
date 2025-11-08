package com.example.mediplan.agenda.disponibilite.dto;

import com.example.mediplan.agenda.disponibilite.Recurrence;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DisponibiliteResponse {

    private final String id;
    private final String medecinId;
    private final LocalDate date;
    private final LocalTime heureDebut;
    private final LocalTime heureFin;
    private final boolean actif;
    private final Recurrence recurrence;
    private final String commentaire;
    private final Instant createdAt;
    private final Instant updatedAt;
}
