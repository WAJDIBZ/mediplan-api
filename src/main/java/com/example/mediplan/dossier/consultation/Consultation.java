package com.example.mediplan.dossier.consultation;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "consultation")
public class Consultation {

    @Id
    private String id;

    private String rendezVousId;

    private String medecinId;

    private String patientId;

    private Instant date;

    private String resume;

    private String diagnostic;

    private String planSuivi;

    private List<String> recommandations;

    @CreatedDate
    private Instant createdAt;
}
