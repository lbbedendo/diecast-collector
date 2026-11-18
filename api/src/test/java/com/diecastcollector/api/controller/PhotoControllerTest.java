package com.diecastcollector.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.diecastcollector.api.AbstractIntegrationTest;
import com.diecastcollector.api.dto.ModelRequest;
import com.diecastcollector.api.dto.ModelResponse;
import com.diecastcollector.api.dto.PhotoUploadResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

class PhotoControllerTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void uploadedPhotoIsServedBack() {
        var headers = authHeaders();
        ModelResponse model = restTemplate.exchange(
                "/models", HttpMethod.POST,
                new HttpEntity<>(new ModelRequest("Photo test", null, null, null, null, null, null, false,
                        null, null, null, null, null, null), headers),
                ModelResponse.class).getBody();
        byte[] photo = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3, (byte) 0xFF, (byte) 0xD9};

        var form = new LinkedMultiValueMap<String, Object>();
        form.add("file", new ByteArrayResource(photo) {
            @Override
            public String getFilename() {
                return "photo.jpg";
            }
        });
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var upload = restTemplate.exchange(
                "/models/" + model.id() + "/photo", HttpMethod.POST, new HttpEntity<>(form, headers),
                PhotoUploadResponse.class);
        assertThat(upload.getStatusCode()).isEqualTo(HttpStatus.OK);

        // With the default, relative photos-dir ("./data/photos"), every photo used to 404 here.
        var served = restTemplate.getForEntity(upload.getBody().photoUrl(), byte[].class);
        assertThat(served.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(served.getBody()).isEqualTo(photo);
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
