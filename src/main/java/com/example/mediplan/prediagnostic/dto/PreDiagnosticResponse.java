package com.example.mediplan.prediagnostic.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreDiagnosticResponse {

    private final String conclusion;
    private final List<String> recommandations;
}
