package com.example.mediplan.agenda.rendezvous.dto;

import com.example.mediplan.agenda.rendezvous.RendezVousStatut;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangementStatutRequest {

    @NotNull(message = "Le statut est obligatoire")
    private RendezVousStatut statut;

    private String commentaire;
}
