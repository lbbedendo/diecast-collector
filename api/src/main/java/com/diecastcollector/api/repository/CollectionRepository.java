package com.diecastcollector.api.repository;

import com.diecastcollector.api.domain.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByOrderByNameAsc();
}
