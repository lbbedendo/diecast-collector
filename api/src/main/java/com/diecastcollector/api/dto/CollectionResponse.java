package com.diecastcollector.api.dto;

import com.diecastcollector.api.domain.Collection;

public record CollectionResponse(Long id, String name, Integer year) {
    public static CollectionResponse from(Collection collection) {
        return new CollectionResponse(collection.getId(), collection.getName(), collection.getYear());
    }
}
