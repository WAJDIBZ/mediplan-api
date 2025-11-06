package com.example.mediplan.security.oauth;

import com.example.mediplan.user.Patient;
import com.example.mediplan.user.Role;
import com.example.mediplan.user.User;
import com.example.mediplan.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        SocialProfile profile = extractProfile(registrationId, attributes);
        User persisted = upsertUser(registrationId, profile);

        Map<String, Object> enriched = new HashMap<>(attributes);
        enriched.put("provider", registrationId.toUpperCase(Locale.ROOT));
        enriched.put("providerId", profile.id());
        enriched.put("email", persisted.getEmail());
        enriched.put("name", persisted.getFullName());
        enriched.put("avatarUrl", persisted.getAvatarUrl());
        enriched.put("userId", persisted.getId());

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + persisted.getRole().name())),
                enriched,
                nameAttributeKey
        );
    }

    private SocialProfile extractProfile(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return extractFromGoogle(attributes);
        }
        if ("facebook".equalsIgnoreCase(registrationId)) {
            return extractFromFacebook(attributes);
        }
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
                "Provider OAuth2 non supporté : " + registrationId);
    }

    private SocialProfile extractFromGoogle(Map<String, Object> attributes) {
        String sub = asString(attributes.get("sub"));
        if (!StringUtils.hasText(sub)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_google_response"),
                    "Réponse Google invalide : identifiant manquant");
        }
        String email = normalizeEmail(asString(attributes.get("email")));
        String name = defaultName(asString(attributes.get("name")), String.format(Locale.ROOT, "Google-%s", sub));
        String avatar = asString(attributes.get("picture"));
        return new SocialProfile(sub, name, email, avatar);
    }

    @SuppressWarnings("unchecked")
    private SocialProfile extractFromFacebook(Map<String, Object> attributes) {
        String id = asString(attributes.get("id"));
        if (!StringUtils.hasText(id)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_facebook_response"),
                    "Réponse Facebook invalide : identifiant manquant");
        }
        String email = normalizeEmail(asString(attributes.get("email")));
        String name = defaultName(asString(attributes.get("name")), String.format(Locale.ROOT, "Facebook-%s", id));
        String avatar = null;
        Object picture = attributes.get("picture");
        if (picture instanceof Map<?, ?> pictureMap) {
            Object data = pictureMap.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                avatar = asString(dataMap.get("url"));
            }
        }
        return new SocialProfile(id, name, email, avatar);
    }

    private User upsertUser(String registrationId, SocialProfile profile) {
        String provider = registrationId.toUpperCase(Locale.ROOT);
        String normalizedEmail = profile.email();
        Optional<User> byEmail = normalizedEmail != null
                ? userService.findByEmail(normalizedEmail)
                : Optional.empty();
        Optional<User> byProvider = userService.findByProviderAndProviderId(provider, profile.id());

        User user = byEmail.or(() -> byProvider).orElse(null);

        if (user == null) {
            user = Patient.builder()
                    .fullName(profile.displayName())
                    .email(normalizedEmail)
                    .avatarUrl(profile.avatarUrl())
                    .passwordHash(null)
                    .role(Role.PATIENT)
                    .emailVerified(true)
                    .provider(provider)
                    .providerId(profile.id())
                    .build();
            return userService.save(user);
        }

        boolean dirty = false;
        if (normalizedEmail != null && !Objects.equals(user.getEmail(), normalizedEmail)) {
            user.setEmail(normalizedEmail);
            dirty = true;
        }
        if (profile.displayName() != null && !Objects.equals(user.getFullName(), profile.displayName())) {
            user.setFullName(profile.displayName());
            dirty = true;
        }
        if (profile.avatarUrl() != null && !Objects.equals(user.getAvatarUrl(), profile.avatarUrl())) {
            user.setAvatarUrl(profile.avatarUrl());
            dirty = true;
        }
        if (!Objects.equals(user.getProvider(), provider)) {
            user.setProvider(provider);
            dirty = true;
        }
        if (!Objects.equals(user.getProviderId(), profile.id())) {
            user.setProviderId(profile.id());
            dirty = true;
        }
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            dirty = true;
        }
        if (user.getRole() == null) {
            user.setRole(Role.PATIENT);
            dirty = true;
        }

        return dirty ? userService.save(user) : user;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String defaultName(String candidate, String fallback) {
        if (StringUtils.hasText(candidate)) {
            return candidate.trim();
        }
        return fallback;
    }

    private record SocialProfile(String id, String displayName, String email, String avatarUrl) {
    }
}
