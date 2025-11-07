package com.example.mediplan.security.oauth;

import com.example.mediplan.security.jwt.JwtService;
import com.example.mediplan.user.Patient;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @GetMapping("/callback")
    public ResponseEntity<?> googleCallback(@RequestParam String code) {
        try {
            // 1️⃣ Exchange the code for access token
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = new HashMap<>();
            body.put("code", code);
            body.put("client_id", "YOUR_GOOGLE_CLIENT_ID");
            body.put("client_secret", "YOUR_GOOGLE_CLIENT_SECRET");
            body.put("redirect_uri", "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/google/callback");
            body.put("grant_type", "authorization_code");

            Map<String, Object> tokenResponse = restTemplate.postForObject(TOKEN_URL, body, Map.class);
            String accessToken = (String) tokenResponse.get("access_token");

            // 2️⃣ Fetch user info
            String url = USERINFO_URL + "?access_token=" + accessToken;
            Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");

            // 3️⃣ Check if user exists or create one
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User u = new Patient();
                        u.setEmail(email);
                        u.setFullName(name);
                        u.setProvider("google");
                        return userRepository.save(u);
                    });

            // 4️⃣ Issue your JWT
            String jwt = jwtService.generateAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole() != null ? user.getRole().name() : "USER"
            );


            // 5️⃣ Send back to frontend with JWT
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("email", email);
            response.put("name", name);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> redirectToGoogle() {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=YOUR_GOOGLE_CLIENT_ID" +
                "&redirect_uri=https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/google/callback" +
                "&response_type=code&scope=openid%20email%20profile";
        return ResponseEntity.ok(Map.of("url", googleAuthUrl));
    }
}
