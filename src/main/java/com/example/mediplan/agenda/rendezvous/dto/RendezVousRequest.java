package com.example.mediplan.agenda.rendezvous.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RendezVousRequest {

    @NotBlank(message = "L'identifiant du médecin est obligatoire")
    private String medecinId;

    @NotBlank(message = "L'identifiant du patient est obligatoire")
    private String patientId;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "Le rendez-vous doit être planifié dans le futur")
    private Instant debut;

    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "Le rendez-vous doit être planifié dans le futur")
    private Instant fin;

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String motif;

    @Size(max = 2000, message = "Les notes ne doivent pas dépasser 2000 caractères")
    private String notesPrivees;
}
