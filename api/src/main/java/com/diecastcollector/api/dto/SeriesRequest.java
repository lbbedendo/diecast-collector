package com.diecastcollector.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeriesRequest(@NotNull Long brandId, @NotBlank String name, Integer year) {}
