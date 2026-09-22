package com.diecastcollector.api.dto;

import com.diecastcollector.api.enums.ModelCondition;
import com.diecastcollector.api.enums.ModelScale;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ModelRequest(
        @NotEmpty String name,
        Integer modelYear,
        ModelScale scale,
        String color,
        ModelCondition condition,
        String seriesName,
        String seriesNumber,
        boolean chase,
        BigDecimal purchasePrice,
        LocalDate purchaseDate,
        String purchasedFrom,
        String notes,
        Long automakerId,
        Long brandId,
        Long collectionId) {}
