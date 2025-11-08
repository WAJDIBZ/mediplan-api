package com.example.mediplan.dossier.prescription.dto;

import com.example.mediplan.dossier.prescription.Medication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrescriptionRequest {

    @NotBlank(message = "La consultation est obligatoire")
    private String consultationId;

    @NotBlank(message = "Le patient est obligatoire")
    private String patientId;

    @NotEmpty(message = "Au moins un médicament doit être renseigné")
    private List<@Valid MedicationRequest> medicaments;

    @Size(max = 1000, message = "Les instructions générales sont trop longues")
    private String instructionsGenerales;

    @Getter
    @Setter
    public static class MedicationRequest {
        @NotBlank(message = "Le nom du médicament est obligatoire")
        private String nom;
        @NotBlank(message = "Le dosage est obligatoire")
        private String dosage;
        @NotBlank(message = "La fréquence est obligatoire")
        private String frequence;
        @NotBlank(message = "La durée est obligatoire")
        private String duree;

        public Medication toMedication() {
            return Medication.builder()
                    .nom(nom)
                    .dosage(dosage)
                    .frequence(frequence)
                    .duree(duree)
                    .build();
        }
    }
}
