package com.example.mediplan.prediagnostic;

import com.example.mediplan.prediagnostic.dto.PreDiagnosticRequest;
import com.example.mediplan.prediagnostic.dto.PreDiagnosticResponse;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PreDiagnosticService {

    public PreDiagnosticResponse analyser(@Valid PreDiagnosticRequest request) {
        List<String> recommandations = new ArrayList<>();
        String conclusion = "Symptômes légers détectés. Consultez votre médecin si cela persiste.";
        String symptoms = String.join(" ", request.getSymptomes()).toLowerCase(Locale.FRENCH);
        if (symptoms.contains("fièvre") || symptoms.contains("fievre")) {
            recommandations.add("Surveillez votre température toutes les 4 heures");
        }
        if (symptoms.contains("douleur") || symptoms.contains(" douleur")) {
            recommandations.add("Si la douleur est aiguë ou persistante, contactez immédiatement un professionnel");
        }
        if (symptoms.contains("difficulté respiratoire") || symptoms.contains("essoufflement")) {
            conclusion = "Symptômes potentiellement graves. Rendez-vous aux urgences ou contactez le 15.";
        }
        recommandations.add("Ce pré-diagnostic ne remplace pas un avis médical. En cas de doute, consultez." );
        return PreDiagnosticResponse.builder()
                .conclusion(conclusion)
                .recommandations(recommandations)
                .build();
    }
}
