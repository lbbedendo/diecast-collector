package com.diecastcollector.api.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Serves photos stored by {@link com.diecastcollector.api.service.PhotoStorageService}. */
@RestController
public class PhotoController {

    private final Path photosDir;

    public PhotoController(@Value("${app.storage.photos-dir}") String photosDir) {
        this.photosDir = Path.of(photosDir);
    }

    @GetMapping("/photos/{filename}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String filename) throws IOException {
        Path file = photosDir.resolve(filename).normalize();
        if (!file.startsWith(photosDir) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(file.toUri());
        String contentType = Files.probeContentType(file);
        return ResponseEntity.ok()
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .body(resource);
    }
}
