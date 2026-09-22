package com.diecastcollector.api.repository;

import com.diecastcollector.api.domain.Automaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomakerRepository extends JpaRepository<Automaker, Long> {
    List<Automaker> findAllByOrderByNameAsc();
}
