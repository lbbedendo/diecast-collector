package com.diecastcollector.api.repository;

import com.diecastcollector.api.domain.User;
import com.diecastcollector.api.enums.AuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderUid(AuthProvider provider, String providerUid);
}
