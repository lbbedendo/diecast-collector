package com.diecastcollector.api.domain;

import com.diecastcollector.api.enums.AuthProvider;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_uid"}))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_uid", nullable = false)
    private String providerUid;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {}

    /** Lightweight reference constructor, e.g. to set an owner FK without loading the full row. */
    public User(Long id) {
        this.id = id;
    }

    public User(AuthProvider provider, String providerUid, String email, String displayName) {
        this.provider = provider;
        this.providerUid = providerUid;
        this.email = email;
        this.displayName = displayName;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
