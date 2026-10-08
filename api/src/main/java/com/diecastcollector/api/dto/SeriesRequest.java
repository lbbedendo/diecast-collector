package com.diecastcollector.api.dto;

import jakarta.validation.constraints.NotBlank;

public record SeriesRequest(@NotBlank String name, Integer year) {}
