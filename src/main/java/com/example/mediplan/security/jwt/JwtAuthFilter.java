package com.example.mediplan.security.jwt;

import com.example.mediplan.user.User;
import com.example.mediplan.user.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicPath(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtService.parseAccessToken(token).getBody();
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (!StringUtils.hasText(userId) || !StringUtils.hasText(role)) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide");
                return;
            }

            User user = userService.findById(userId)
                    .orElse(null);
            if (user == null) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Utilisateur introuvable");
                return;
            }

            if (!user.isActive()) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "Compte désactivé. Contactez l'administrateur.");
                return;
            }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("userId", user.getId());

        } catch (JwtException ex) {
            LOGGER.warn("Token JWT invalide : {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide");
            return;
        } catch (Exception ex) {
            LOGGER.error("Erreur lors de l'authentification JWT", ex);
            SecurityContextHolder.clearContext();
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || "/error".equals(path)
                || "/data-deletion.html".equals(path)
                || "/privacy-policy".equals(path);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String safeMessage = message.replace("\"", "\\\"");
        response.getWriter().write("{\"message\":\"" + safeMessage + "\"}");
    }
}
