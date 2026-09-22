package com.diecastcollector.api.security;

/** The identity claims we trust once a provider's ID token has been signature- and audience-verified. */
public record VerifiedIdentity(String providerUid, String email, String displayName) {}
