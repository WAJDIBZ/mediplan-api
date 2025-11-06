package com.example.mediplan.security.oauth;

import com.example.mediplan.security.jwt.JwtService;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;
    private final String redirectMode;
    private final String frontRedirectUrl;

    public OAuth2LoginSuccessHandler(JwtService jwtService,
                                     UserService userService,
                                     @Value("${app.oauth.redirect-mode:QUERY}") String redirectMode) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.redirectMode = redirectMode == null ? "QUERY" : redirectMode.trim();
        this.frontRedirectUrl = Optional.ofNullable(System.getenv("FRONT_REDIRECT_URL"))
                .orElse("http://localhost:3000/oauth/success");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification OAuth2 invalide");
            return;
        }
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase(Locale.ROOT);
        String providerId = principal.getAttribute("providerId");
        String email = principal.getAttribute("email");

        Optional<User> resolved = resolveUser(provider, providerId, email);
        if (resolved.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Utilisateur OAuth2 introuvable");
            return;
        }
        User user = resolved.get();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        if ("COOKIES".equalsIgnoreCase(redirectMode)) {
            boolean secure = request.isSecure() || frontRedirectUrl.startsWith("https");
            addCookie(response, "accessToken", accessToken, Duration.ofMinutes(jwtService.getAccessExpMin()), secure);
            addCookie(response, "refreshToken", refreshToken, Duration.ofDays(jwtService.getRefreshExpDays()), secure);
            response.sendRedirect(frontRedirectUrl);
        } else {
            response.sendRedirect(buildQueryRedirect(accessToken, refreshToken));
        }
    }

    private Optional<User> resolveUser(String provider, String providerId, String email) {
        Optional<User> byEmail = StringUtils.hasText(email) ? userService.findByEmail(email) : Optional.empty();
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return userService.findByProviderAndProviderId(provider, providerId);
    }

    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge, boolean secure) {
        String sameSite = secure ? "None" : "Lax";
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String buildQueryRedirect(String accessToken, String refreshToken) {
        StringBuilder redirect = new StringBuilder(frontRedirectUrl);
        String delimiter = frontRedirectUrl.contains("?") ? "&" : "?";
        redirect.append(delimiter)
                .append("accessToken=").append(urlEncode(accessToken))
                .append("&refreshToken=").append(urlEncode(refreshToken));
        return redirect.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
