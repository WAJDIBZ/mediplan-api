package com.example.mediplan.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmergencyContactDTO {

    @NotBlank(message = "Le nom du contact est obligatoire")
    @Schema(example = "Jean Dupont")
    private String name;

    @NotBlank(message = "Le numéro du contact est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Le numéro doit être au format international")
    @Schema(example = "+33123456789")
    private String phone;

    @NotBlank(message = "La relation est obligatoire")
    @Schema(example = "Conjoint")
    private String relation;
}
