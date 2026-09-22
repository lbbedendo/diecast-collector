package com.diecastcollector.api.dto;

import com.diecastcollector.api.domain.Automaker;

public record AutomakerResponse(Long id, String name, String country) {
    public static AutomakerResponse from(Automaker automaker) {
        return new AutomakerResponse(automaker.getId(), automaker.getName(), automaker.getCountry());
    }
}
