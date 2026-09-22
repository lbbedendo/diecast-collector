package com.diecastcollector.api.dto;

import jakarta.validation.constraints.NotBlank;

/** The raw ID token obtained on-device from Google Sign-In or Sign in with Apple. */
public record SocialLoginRequest(@NotBlank String idToken) {}
