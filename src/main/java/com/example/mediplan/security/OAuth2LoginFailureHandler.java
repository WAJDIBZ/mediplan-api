package com.example.mediplan.security;

import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
                                        AuthenticationException ex) {
        ex.printStackTrace(); // shows up in Heroku logs
        try {
            String msg = java.net.URLEncoder.encode(ex.getMessage(), "UTF-8");
            getRedirectStrategy().sendRedirect(req, res, "/login?oauth2_error=" + msg);
        } catch (Exception ignored) {}
    }
}
