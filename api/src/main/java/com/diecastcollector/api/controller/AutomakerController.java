package com.diecastcollector.api.controller;

import com.diecastcollector.api.dto.AutomakerRequest;
import com.diecastcollector.api.dto.AutomakerResponse;
import com.diecastcollector.api.service.AutomakerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/automakers")
public class AutomakerController {

    private final AutomakerService automakerService;

    public AutomakerController(AutomakerService automakerService) {
        this.automakerService = automakerService;
    }

    @GetMapping
    public List<AutomakerResponse> getAll() {
        return automakerService.findAll().stream().map(AutomakerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AutomakerResponse getById(@PathVariable Long id) {
        return AutomakerResponse.from(automakerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AutomakerResponse> create(@Valid @RequestBody AutomakerRequest request) {
        var automaker = automakerService.create(request);
        return ResponseEntity.created(URI.create("/automakers/" + automaker.getId()))
                .body(AutomakerResponse.from(automaker));
    }

    @PutMapping("/{id}")
    public AutomakerResponse update(@PathVariable Long id, @Valid @RequestBody AutomakerRequest request) {
        return AutomakerResponse.from(automakerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        automakerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
