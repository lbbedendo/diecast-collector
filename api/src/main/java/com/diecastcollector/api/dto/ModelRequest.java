package com.diecastcollector.api.dto;

import com.diecastcollector.api.enums.ModelCondition;
import com.diecastcollector.api.enums.ModelPackaging;
import com.diecastcollector.api.enums.ModelScale;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ModelRequest(
        @NotEmpty String name,
        Integer vehicleYear,
        ModelScale scale,
        String color,
        ModelPackaging packaging,
        ModelCondition condition,
        String seriesNumber,
        boolean chase,
        BigDecimal purchasePrice,
        LocalDate purchaseDate,
        String purchasedFrom,
        String notes,
        Long automakerId,
        Long seriesId) {}
