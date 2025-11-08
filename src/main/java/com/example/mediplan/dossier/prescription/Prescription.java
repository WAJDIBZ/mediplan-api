package com.example.mediplan.dossier.prescription;

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
@Document(collection = "prescription")
public class Prescription {

    @Id
    private String id;

    private String consultationId;

    private String medecinId;

    private String patientId;

    private List<Medication> medicaments;

    private String instructionsGenerales;

    @CreatedDate
    private Instant createdAt;
}
