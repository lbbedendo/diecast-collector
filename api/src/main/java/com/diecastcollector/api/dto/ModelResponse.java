package com.diecastcollector.api.dto;

import com.diecastcollector.api.domain.Model;
import com.diecastcollector.api.enums.ModelCondition;
import com.diecastcollector.api.enums.ModelPackaging;
import com.diecastcollector.api.enums.ModelScale;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ModelResponse(
        Long id,
        String name,
        Integer modelYear,
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
        String photoUrl,
        AutomakerResponse automaker,
        SeriesResponse series) {

    public static ModelResponse from(Model model) {
        return new ModelResponse(
                model.getId(),
                model.getName(),
                model.getModelYear(),
                model.getScale(),
                model.getColor(),
                model.getPackaging(),
                model.getCondition(),
                model.getSeriesNumber(),
                model.isChase(),
                model.getPurchasePrice(),
                model.getPurchaseDate(),
                model.getPurchasedFrom(),
                model.getNotes(),
                model.getPhotoUrl(),
                model.getAutomaker() != null ? AutomakerResponse.from(model.getAutomaker()) : null,
                model.getSeries() != null ? SeriesResponse.from(model.getSeries()) : null);
    }
}
