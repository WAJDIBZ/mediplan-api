package com.example.mediplan.dossier.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultationRequest {

    @NotBlank(message = "L'identifiant du rendez-vous est obligatoire")
    private String rendezVousId;

    @NotBlank(message = "L'identifiant du patient est obligatoire")
    private String patientId;

    @NotNull(message = "La date est obligatoire")
    private Instant date;

    @Size(max = 1000, message = "Le résumé ne doit pas dépasser 1000 caractères")
    private String resume;

    @Size(max = 2000, message = "Le diagnostic ne doit pas dépasser 2000 caractères")
    private String diagnostic;

    @Size(max = 2000, message = "Le plan de suivi ne doit pas dépasser 2000 caractères")
    private String planSuivi;

    private List<@Size(max = 255, message = "Chaque recommandation est limitée à 255 caractères") String> recommandations;
}
