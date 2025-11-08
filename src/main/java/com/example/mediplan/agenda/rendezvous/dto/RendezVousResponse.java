package com.example.mediplan.agenda.rendezvous.dto;

import com.example.mediplan.agenda.rendezvous.RendezVousStatut;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RendezVousResponse {

    private final String id;
    private final String medecinId;
    private final String patientId;
    private final Instant debut;
    private final Instant fin;
    private final RendezVousStatut statut;
    private final String motif;
    private final String notesPrivees;
    private final String createurId;
    private final Instant createdAt;
    private final Instant updatedAt;
}
