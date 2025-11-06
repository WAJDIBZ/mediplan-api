package com.example.mediplan.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank(message = "La ligne d'adresse est obligatoire")
    @Schema(example = "123 rue de la Santé")
    private String line1;

    @Schema(example = "Appartement 4B")
    private String line2;

    @NotBlank(message = "La ville est obligatoire")
    @Schema(example = "Paris")
    private String city;

    @NotBlank(message = "Le pays est obligatoire")
    @Schema(example = "France")
    private String country;

    @NotBlank(message = "Le code postal est obligatoire")
    @Schema(example = "75013")
    private String zip;
}
