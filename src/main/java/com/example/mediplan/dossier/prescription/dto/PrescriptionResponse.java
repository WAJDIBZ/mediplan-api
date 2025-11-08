package com.example.mediplan.dossier.prescription.dto;

import com.example.mediplan.dossier.prescription.Medication;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrescriptionResponse {

    private final String id;
    private final String consultationId;
    private final String medecinId;
    private final String patientId;
    private final List<Medication> medicaments;
    private final String instructionsGenerales;
    private final Instant createdAt;
}
