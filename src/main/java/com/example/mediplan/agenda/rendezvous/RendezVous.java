package com.example.mediplan.agenda.rendezvous;

import java.time.Instant;

import com.example.mediplan.user.Medecin;
import com.example.mediplan.user.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "rendezvous")
@CompoundIndex(name = "idx_rdv_medecin_creneau", def = "{ 'medecinId': 1, 'debut': 1, 'fin': 1 }")
public class RendezVous {

    @Id
    private String id;

    @DBRef
    private Medecin medecin;

    @DBRef
    private Patient patient;

    private Instant debut;

    private Instant fin;

    private RendezVousStatut statut;

    private String motif;

    private String notesPrivees;

    private String createurId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
