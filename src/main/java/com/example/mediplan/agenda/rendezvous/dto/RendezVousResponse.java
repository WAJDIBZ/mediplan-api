package com.example.mediplan.agenda.rendezvous.dto;

import com.example.mediplan.agenda.rendezvous.RendezVousStatut;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RendezVousResponse {
    private String id;

    // Keep these for backward compatibility
    private String medecinId;
    private String patientId;

    // ✅ Add these new fields
    private ParticipantDTO medecin;
    private ParticipantDTO patient;

    private Instant debut;
    private Instant fin;
    private RendezVousStatut statut;
    private String motif;
    private String notesPrivees;
    private String createurId;
    private Instant createdAt;
    private Instant updatedAt;
}