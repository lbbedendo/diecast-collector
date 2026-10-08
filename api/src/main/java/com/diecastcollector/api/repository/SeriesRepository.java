package com.diecastcollector.api.repository;

import com.diecastcollector.api.domain.Series;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    @EntityGraph(attributePaths = "brand")
    List<Series> findAllByOrderByNameAsc();

    @Override
    @EntityGraph(attributePaths = "brand")
    Optional<Series> findById(Long id);

    Optional<Series> findByBrandIdAndNameAndYear(Long brandId, String name, Integer year);

    Optional<Series> findByBrandIdAndNameAndYearIsNull(Long brandId, String name);
}
