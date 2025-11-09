package com.example.mediplan.dossier.prescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medication {

    private String nom;
    private String dosage;
    private String frequence;
    private String duree;
}
