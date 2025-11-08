package com.example.mediplan.medecin.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedecinSearchRequest {

    @Size(max = 80, message = "La recherche est trop longue")
    private String q;

    private String specialite;

    private String ville;
}
