package com.diecastcollector.api.service;

import com.diecastcollector.api.domain.Automaker;
import com.diecastcollector.api.dto.AutomakerRequest;
import com.diecastcollector.api.exception.ResourceNotFoundException;
import com.diecastcollector.api.repository.AutomakerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomakerService {

    private final AutomakerRepository automakerRepository;

    public AutomakerService(AutomakerRepository automakerRepository) {
        this.automakerRepository = automakerRepository;
    }

    public List<Automaker> findAll() {
        return automakerRepository.findAllByOrderByNameAsc();
    }

    public Automaker getById(Long id) {
        return automakerRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Automaker create(AutomakerRequest request) {
        return automakerRepository.save(new Automaker(request.name(), request.country()));
    }

    @Transactional
    public Automaker update(Long id, AutomakerRequest request) {
        Automaker automaker = getById(id);
        automaker.setName(request.name());
        automaker.setCountry(request.country());
        return automakerRepository.save(automaker);
    }

    @Transactional
    public void delete(Long id) {
        Automaker automaker = getById(id);
        automakerRepository.delete(automaker);
    }

    private ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException("Automaker " + id + " not found");
    }
}
