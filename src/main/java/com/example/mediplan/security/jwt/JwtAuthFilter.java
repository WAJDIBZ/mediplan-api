package com.example.mediplan.security.jwt;

import com.example.mediplan.user.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String path = request.getRequestURI();
        final String method = request.getMethod();

        // ✅ Always skip CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Public endpoints (no auth required)
        if (path.startsWith("/api/auth/")
                || path.startsWith("/actuator/")
                || "/error".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ CRITICAL: let Spring Security OAuth2 handle these fully
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ If there is no Bearer token, do not block — just continue
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Parse & set authentication when token is valid; otherwise just continue
        try {
            final String token = authHeader.substring(7);
            Claims claims = jwtService.parseAccessToken(token).getBody();
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            userRepository.findById(userId).ifPresent(u -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        u.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("userId", u.getId());
            });
        } catch (Exception ignored) {
            // Do not fail the request here; security rules will handle unauthenticated access.
        }

        chain.doFilter(request, response);
    }
}
