package com.mobasserazaman.event_ticket_engine.Controller;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", jwt.getSubject());

        addClaim(profile, "username", jwt.getClaimAsString("preferred_username"));
        addClaim(profile, "email", jwt.getClaimAsString("email"));
        addClaim(profile, "name", jwt.getClaimAsString("name"));

        Object realmAccessClaim = jwt.getClaims().get("realm_access");
        if (realmAccessClaim instanceof Map<?, ?> realmAccess) {
            Object roles = realmAccess.get("roles");
            if (roles instanceof Collection<?> roleList) {
                profile.put("roles", roleList);
            }
        }

        return profile;
    }

    private void addClaim(Map<String, Object> profile, String key, String value) {
        if (value != null) {
            profile.put(key, value);
        }
    }

}
