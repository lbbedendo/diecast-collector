package com.diecastcollector.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CollectionRequest(@NotBlank String name, Integer year) {}
