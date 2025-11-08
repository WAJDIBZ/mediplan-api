package com.example.mediplan.security;

import java.time.Duration;
import java.util.List;

import com.example.mediplan.config.CorsProperties;
import com.example.mediplan.security.jwt.JwtAuthFilter;
import com.example.mediplan.security.oauth.CustomOAuth2UserService;
import com.example.mediplan.security.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> buildCorsConfiguration()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/actuator/**", "/data-deletion.html",
                                "/privacy-policy", "/error").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = corsProperties.getAllowedOrigins();
        if (origins == null || origins.isEmpty()) {
            configuration.addAllowedOriginPattern("*");
        } else {
            configuration.setAllowedOrigins(origins);
        }

        List<String> methods = corsProperties.getAllowedMethods();
        if (methods == null || methods.isEmpty()) {
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        } else {
            configuration.setAllowedMethods(methods);
        }

        List<String> headers = corsProperties.getAllowedHeaders();
        if (headers == null || headers.isEmpty()) {
            configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        } else {
            configuration.setAllowedHeaders(headers);
        }
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(Duration.ofHours(1));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        return configuration;
    }
}
