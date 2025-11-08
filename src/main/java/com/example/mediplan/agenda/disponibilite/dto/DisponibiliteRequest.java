package com.example.mediplan.agenda.disponibilite.dto;

import com.example.mediplan.agenda.disponibilite.Recurrence;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisponibiliteRequest {

    @NotBlank(message = "L'identifiant du médecin est obligatoire")
    private String medecinId;

    @NotNull(message = "La date est obligatoire")
    @FutureOrPresent(message = "La date doit être future ou du jour")
    private LocalDate date;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    private Recurrence recurrence = Recurrence.AUCUNE;

    @Size(max = 255, message = "Le commentaire ne doit pas dépasser 255 caractères")
    private String commentaire;
}
