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

/**
 * Интеграционные тесты для PhotoController.
 * Проверяют полный сценарий: загрузка, скачивание и удаление фотографии,
 * а также обработку неподдерживаемого формата.
 */
@SpringBootTest
// @ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username="admin", roles={"USER","ADMIN"})
class PhotoControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepo;

    private Long userId;
    private byte[] imgBytes;

    /**
     * Подготовка данных:
     * 1) Очищаем репозиторий пользователей.
     * 2) Создаём нового пользователя и сохраняем его ID.
     * 3) Загружаем тестовый jpeg-файл в массив байт.
     */
    @BeforeEach
    void setup() throws Exception {
        userRepo.deleteAll();

        User u = new User();
        u.setUsername("u1");
        u.setPassword("x"); // будет захеширован при сохранении
        userRepo.save(u);
        userId = userRepo.findByUsername("u1").get().getId();

        imgBytes = FileUtils.readFileToByteArray(
                Path.of("src/test/resources/test-image.jpg").toFile()
        );
    }

    /**
     * Сценарий «загрузить → скачать → удалить → убедиться, что нет».
     * Проверяет, что:
     * - загрузка возвращает 200 OK
     * - скачивание возвращает 200 OK и точный контент
     * - удаление возвращает 204 No Content
     * - повторный запрос на скачивание даёт 404 Not Found
     */
    @Test
    void uploadAndDownloadAndDeletePhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, imgBytes
        );

        // Загрузка
        mvc.perform(multipart("/api/users/{id}/photo", userId).file(file))
                .andExpect(status().isOk());

        // Скачивание
        mvc.perform(get("/api/users/{id}/photo", userId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(imgBytes));

        // Удаление
        mvc.perform(delete("/api/users/{id}/photo", userId))
                .andExpect(status().isNoContent());

        // После удаления — 404 Not Found
        mvc.perform(get("/api/users/{id}/photo", userId))
                .andExpect(status().isNotFound());
    }

    /**
     * Попытка загрузить файл неподдерживаемого типа (text/plain)
     * должна вернуть 400 Bad Request.
     */
    @Test
    void uploadWrongType_thenBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.txt", MediaType.TEXT_PLAIN_VALUE, "oops".getBytes()
        );

        mvc.perform(multipart("/api/users/{id}/photo", userId).file(file))
                .andExpect(status().isBadRequest());
    }
}