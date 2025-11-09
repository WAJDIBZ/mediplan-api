package com.example.mediplan.agenda.disponibilite;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "disponibilite")
@CompoundIndex(name = "idx_dispo_medecin_date", def = "{ 'medecinId': 1, 'date': 1 }")
public class Disponibilite {

    @Id
    private String id;

    private String medecinId;

    private LocalDate date;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    @Builder.Default
    private boolean actif = true;

    private Recurrence recurrence;

    private String commentaire;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}
