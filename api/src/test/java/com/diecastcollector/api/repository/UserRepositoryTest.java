package com.diecastcollector.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.enums.AuthProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User has no REST controller of its own (it's only ever created via social login, see
 * {@link com.diecastcollector.api.controller.AuthController}), so this exercises the repository
 * directly against the real database instead of going through HTTP like the other domain classes.
 */
class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired private UserRepository userRepository;

    @Test
    void createUser() {
        String providerUid = "uid-" + UUID.randomUUID();

        User saved = userRepository.save(new User(AuthProvider.GOOGLE, providerUid, "ferrari.fan@example.com", "Ferrari Fan"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(saved.getProviderUid()).isEqualTo(providerUid);
        assertThat(saved.getEmail()).isEqualTo("ferrari.fan@example.com");
        assertThat(saved.getDisplayName()).isEqualTo("Ferrari Fan");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void fetchUser() {
        String providerUid = "uid-" + UUID.randomUUID();
        User saved = userRepository.save(new User(AuthProvider.APPLE, providerUid, "collector@example.com", "Collector"));

        var byId = userRepository.findById(saved.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getEmail()).isEqualTo("collector@example.com");

        var byProviderAndUid = userRepository.findByProviderAndProviderUid(AuthProvider.APPLE, providerUid);
        assertThat(byProviderAndUid).isPresent();
        assertThat(byProviderAndUid.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void updateUser() {
        User saved = userRepository.save(
                new User(AuthProvider.GOOGLE, "uid-" + UUID.randomUUID(), "old@example.com", "Old Name"));

        saved.setEmail("new@example.com");
        saved.setDisplayName("New Name");
        userRepository.save(saved);

        User refetched = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(refetched.getEmail()).isEqualTo("new@example.com");
        assertThat(refetched.getDisplayName()).isEqualTo("New Name");
        // provider/providerUid are immutable after creation (no setters) — confirm they survived the update untouched.
        assertThat(refetched.getProvider()).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    void deleteUser() {
        User saved = userRepository.save(
                new User(AuthProvider.GOOGLE, "uid-" + UUID.randomUUID(), "gone@example.com", "Gone Soon"));

        userRepository.deleteById(saved.getId());

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }
}
