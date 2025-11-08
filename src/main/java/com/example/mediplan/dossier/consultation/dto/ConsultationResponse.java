package com.example.mediplan.dossier.consultation.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationResponse {

    private final String id;
    private final String rendezVousId;
    private final String medecinId;
    private final String patientId;
    private final Instant date;
    private final String resume;
    private final String diagnostic;
    private final String planSuivi;
    private final List<String> recommandations;
    private final Instant createdAt;
}
