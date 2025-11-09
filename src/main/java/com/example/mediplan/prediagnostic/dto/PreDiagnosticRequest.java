package com.example.mediplan.prediagnostic.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreDiagnosticRequest {

    @NotEmpty(message = "Au moins un symptôme est requis")
    private List<@Size(max = 80, message = "Chaque symptôme est limité à 80 caractères") String> symptomes;

    @Size(max = 500, message = "Le contexte est trop long")
    private String contexte;
}
