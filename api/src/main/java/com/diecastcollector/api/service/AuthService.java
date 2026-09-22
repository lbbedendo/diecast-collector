package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.dto.AuthResponse;
import com.diecastcollector.api.dto.UserResponse;
import com.diecastcollector.api.enums.AuthProvider;
import com.diecastcollector.api.repository.UserRepository;
import com.diecastcollector.api.security.AppJwtService;
import com.diecastcollector.api.security.SocialTokenVerifier;
import com.diecastcollector.api.security.VerifiedIdentity;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AppJwtService appJwtService;
    private final Map<AuthProvider, SocialTokenVerifier> verifiersByProvider;

    public AuthService(
            UserRepository userRepository, AppJwtService appJwtService, List<SocialTokenVerifier> verifiers) {
        this.userRepository = userRepository;
        this.appJwtService = appJwtService;
        this.verifiersByProvider =
                verifiers.stream().collect(Collectors.toMap(SocialTokenVerifier::provider, Function.identity()));
    }

    @Transactional
    public AuthResponse login(AuthProvider provider, String idToken) {
        SocialTokenVerifier verifier = verifiersByProvider.get(provider);
        if (verifier == null) {
            throw new IllegalStateException("No token verifier registered for provider " + provider);
        }
        VerifiedIdentity identity = verifier.verify(idToken);

        User user = userRepository
                .findByProviderAndProviderUid(provider, identity.providerUid())
                .map(existing -> refresh(existing, identity))
                .orElseGet(() -> userRepository.save(new User(
                        provider, identity.providerUid(), identity.email(), identity.displayName())));

        String accessToken = appJwtService.issueAccessToken(user);
        return new AuthResponse(accessToken, UserResponse.from(user));
    }

    private User refresh(User user, VerifiedIdentity identity) {
        // Apple only sends email/name on first login; don't blank out what we already have.
        if (identity.email() != null && !identity.email().isBlank()) {
            user.setEmail(identity.email());
        }
        if (identity.displayName() != null && !identity.displayName().isBlank()) {
            user.setDisplayName(identity.displayName());
        }
        return userRepository.save(user);
    }
}
