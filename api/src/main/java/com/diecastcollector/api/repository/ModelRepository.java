package com.diecastcollector.api.repository;

import com.diecastcollector.api.domain.Model;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {

    @EntityGraph(attributePaths = {"automaker", "series", "brand"})
    List<Model> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"automaker", "series", "brand"})
    Optional<Model> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerId(Long ownerId);
}
