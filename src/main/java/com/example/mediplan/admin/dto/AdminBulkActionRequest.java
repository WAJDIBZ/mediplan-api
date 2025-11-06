package com.example.mediplan.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminBulkActionRequest {

    @NotEmpty(message = "La liste des identifiants ne peut pas être vide")
    private List<String> ids;
}
