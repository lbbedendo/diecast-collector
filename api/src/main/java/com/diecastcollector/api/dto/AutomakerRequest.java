package com.diecastcollector.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AutomakerRequest(@NotBlank String name, String country) {}
