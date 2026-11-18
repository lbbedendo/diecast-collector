package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.ModelRequest;
import com.diecastcollector.api.dto.ModelResponse;
import com.diecastcollector.api.dto.PhotoUploadResponse;
import com.diecastcollector.api.service.PhotoStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

class PhotoControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    private static final byte[] PHOTO = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3, (byte) 0xFF, (byte) 0xD9};

    @Test
    void uploadedPhotoIsServedBack() {
        var headers = authHeaders();
        Long modelId = createModel(headers);

        String photoUrl = upload(headers, modelId);

        // With the default, relative photos-dir ("./data/photos"), every photo used to 404 here.
        var served = restTemplate.getForEntity(photoUrl, byte[].class);
        assertThat(served.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(served.getBody()).isEqualTo(PHOTO);
    }

    @Test
    void replacedPhotoIsDeleted() {
        var headers = authHeaders();
        Long modelId = createModel(headers);
        String first = upload(headers, modelId);

        String second = upload(headers, modelId);

        assertThat(restTemplate.getForEntity(first, byte[].class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(restTemplate.getForEntity(second, byte[].class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deletedModelsPhotoIsDeleted() {
        var headers = authHeaders();
        Long modelId = createModel(headers);
        String photoUrl = upload(headers, modelId);

        restTemplate.exchange("/models/" + modelId, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

        assertThat(restTemplate.getForEntity(photoUrl, byte[].class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletingAPhotoNeverLeavesThePhotosDir(@TempDir Path root) throws IOException {
        Path photos = Files.createDirectories(root.resolve("photos"));
        Path secret = Files.writeString(root.resolve("secret.txt"), "not a photo");
        var storage = new PhotoStorageService(photos.toString());

        storage.delete("/photos/../secret.txt");
        storage.delete("/elsewhere/secret.txt");

        assertThat(secret).exists();
    }

    private Long createModel(HttpHeaders headers) {
        return restTemplate.exchange(
                "/models", HttpMethod.POST,
                new HttpEntity<>(new ModelRequest("Photo test", null, null, null, null, null, null, false,
                        null, null, null, null, null, null), headers),
                ModelResponse.class).getBody().id();
    }

    private String upload(HttpHeaders headers, Long modelId) {
        var form = new LinkedMultiValueMap<String, Object>();
        form.add("file", new ByteArrayResource(PHOTO) {
            @Override
            public String getFilename() {
                return "photo.jpg";
            }
        });
        var multipart = new HttpHeaders();
        multipart.putAll(headers);
        multipart.setContentType(MediaType.MULTIPART_FORM_DATA);
        var response = restTemplate.exchange(
                "/models/" + modelId + "/photo", HttpMethod.POST, new HttpEntity<>(form, multipart),
                PhotoUploadResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().photoUrl();
    }

    @Test
    void pathOutsideThePhotosDirIsNotServed(@TempDir Path root) throws IOException {
        // Called directly: over HTTP, Spring's firewall already rejects an encoded "../" with a 400
        // before it reaches the controller, so this checks the controller's own guard.
        Path photos = Files.createDirectories(root.resolve("photos"));
        Files.writeString(root.resolve("secret.txt"), "not a photo");
        var controller = new PhotoController(photos.toString());

        assertThat(controller.getPhoto("../secret.txt").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
