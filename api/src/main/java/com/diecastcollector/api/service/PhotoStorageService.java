package com.diecastcollector.api.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Local-disk photo storage for a first version of the app. The public contract (a stored
 * filename served back as a URL under /photos/**) is what matters for the mobile client; swapping
 * this implementation for S3/GCS later shouldn't require touching controllers or the DB schema.
 */
@Service
public class PhotoStorageService {

    private static final Logger log = LoggerFactory.getLogger(PhotoStorageService.class);
    private static final String URL_PREFIX = "/photos/";

    private final Path photosDir;

    public PhotoStorageService(@Value("${app.storage.photos-dir}") String photosDir) {
        this.photosDir = Path.of(photosDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.photosDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create photos directory: " + this.photosDir, e);
        }
    }

    /** @return the relative URL path the file was stored under, e.g. "/photos/&lt;uuid&gt;.jpg". */
    public String store(MultipartFile file) {
        String extension = extensionOf(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path target = photosDir.resolve(filename);
        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded photo", e);
        }
        return URL_PREFIX + filename;
    }

    /**
     * Deletes the file behind a photo URL returned by {@link #store}, if it is still there. Never
     * throws: a leftover file isn't worth failing the request that replaced or deleted the photo, so
     * a failure is only logged. Ignores anything that isn't a stored photo (null, another URL, a
     * path that climbs out of the photos directory).
     */
    public void delete(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith(URL_PREFIX)) {
            return;
        }
        Path file = photosDir.resolve(photoUrl.substring(URL_PREFIX.length())).normalize();
        if (!file.getParent().equals(photosDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete photo {}", file, e);
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot) : "";
    }
}
