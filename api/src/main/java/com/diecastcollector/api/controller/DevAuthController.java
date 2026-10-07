package com.diecastcollector.api.controller;

import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.dto.AuthResponse;
import com.diecastcollector.api.dto.UserResponse;
import com.diecastcollector.api.enums.AuthProvider;
import com.diecastcollector.api.repository.UserRepository;
import com.diecastcollector.api.security.AppJwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mints a real app JWT for a fixed throwaway user, bypassing Google/Apple ID token verification
 * entirely. Exists only so the mobile app can be exercised end-to-end against a local API before
 * real sign-in is wired up (see app/README.md).
 *
 * <p>Gated by {@code @ConditionalOnProperty} rather than an in-method check: when
 * {@code app.auth.dev-login.enabled} is false (the default), this bean — and therefore the
 * {@code /auth/dev} route — doesn't exist at all, so there's no live endpoint to accidentally
 * hit even if someone guesses the URL. Never set {@code APP_AUTH_DEV_LOGIN_ENABLED=true} outside
 * local development.
 */
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(prefix = "app.auth.dev-login", name = "enabled", havingValue = "true")
public class DevAuthController {

    private static final Logger log = LoggerFactory.getLogger(DevAuthController.class);
    private static final String DEV_PROVIDER_UID = "dev-local-user";

    private final UserRepository userRepository;
    private final AppJwtService appJwtService;

    public DevAuthController(UserRepository userRepository, AppJwtService appJwtService) {
        this.userRepository = userRepository;
        this.appJwtService = appJwtService;
        log.warn(
                "Dev login is ENABLED (app.auth.dev-login.enabled=true) — POST /auth/dev issues a "
                        + "real access token with no credential check. This must never be set in a "
                        + "deployed environment.");
    }

    @PostMapping("/dev")
    public AuthResponse login() {
        User user = userRepository
                .findByProviderAndProviderUid(AuthProvider.GOOGLE, DEV_PROVIDER_UID)
                .orElseGet(() -> userRepository.save(
                        new User(AuthProvider.GOOGLE, DEV_PROVIDER_UID, "dev@localhost", "Dev User")));
        String accessToken = appJwtService.issueAccessToken(user);
        return new AuthResponse(accessToken, UserResponse.from(user));
    }
}
