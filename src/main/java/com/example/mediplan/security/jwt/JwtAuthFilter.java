package com.example.mediplan.security.jwt;

import com.example.mediplan.user.User;
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

        // 1) Preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Public + OAuth2 paths
        if (path.startsWith("/api/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || "/error".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 3) No bearer? just continue (do NOT 401 here)
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 4) Validate token (if present) and continue
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.parseAccessToken(token).getBody();
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            userRepository.findById(userId).ifPresent(u -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        u.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("userId", u.getId());
            });
        } catch (Exception ignored) { /* do not block here */ }

        chain.doFilter(request, response);
    }

}
