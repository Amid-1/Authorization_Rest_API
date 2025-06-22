package web.integration.controller;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import web.model.User;
import web.repository.UserRepository;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
// @ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username="admin", roles={"USER","ADMIN"})
class PhotoControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepo;

    private Long userId;
    private byte[] imgBytes;

    @BeforeEach
    void setup() throws Exception {
        userRepo.deleteAll();
        User u = new User();
        u.setUsername("u1");
        u.setPassword("x"); // хешируется при создании
        userRepo.save(u);
        userId = userRepo.findByUsername("u1").get().getId();

        // какой-нибудь маленький JPEG в ресурсах
        imgBytes = FileUtils.readFileToByteArray(
                Path.of("src/test/resources/test-image.jpg").toFile()
        );
    }

    @Test
    void uploadAndDownloadAndDeletePhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, imgBytes
        );

        // POST upload
        mvc.perform(multipart("/api/users/{id}/photo", userId)
                        .file(file))
                .andExpect(status().isOk());

        // GET download
        mvc.perform(get("/api/users/{id}/photo", userId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(imgBytes));

        // DELETE
        mvc.perform(delete("/api/users/{id}/photo", userId))
                .andExpect(status().isNoContent());

        // 404 после удаления
        mvc.perform(get("/api/users/{id}/photo", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadWrongType_thenBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.txt", MediaType.TEXT_PLAIN_VALUE, "oops".getBytes()
        );

        mvc.perform(multipart("/api/users/{id}/photo", userId)
                        .file(file))
                .andExpect(status().isBadRequest());
    }
}
