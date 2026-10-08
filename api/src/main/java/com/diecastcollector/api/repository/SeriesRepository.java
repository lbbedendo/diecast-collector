package com.diecastcollector.api.repository;

import com.diecastcollector.api.domain.Series;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {
    List<Series> findAllByOrderByNameAsc();
}
