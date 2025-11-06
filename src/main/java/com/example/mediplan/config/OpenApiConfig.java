package com.example.mediplan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiDocumentation() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("MediPlan API")
                        .description("Authentification, inscription et administration des profils (patients, médecins, administrateurs)")
                        .version("1.0.0"));
    }
}
