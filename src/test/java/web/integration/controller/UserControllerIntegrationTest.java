package web.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import web.dto.UserCreateDto;
import web.dto.UserDto;
import web.dto.UserUpdateDto;
import web.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционный тест для UserController.
 * Проверяет полный CRUD-флоу:
 * 1) получение пустого списка
 * 2) создание пользователя
 * 3) чтение созданного
 * 4) обновление
 * 5) удаление
 * 6) проверка, что после удаления — 404
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"USER","ADMIN"})
class UserControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository userRepo;

    /**
     * Очищаем всех пользователей перед каждым тестом.
     */
    @BeforeEach
    void clean() {
        userRepo.deleteAll();
    }

    /**
     * Тестирует полный жизненный цикл пользователя:
     * - GET /api/users → []
     * - POST /api/users → 201 + тело нового пользователя
     * - GET /api/users/{id} → 200 + правильное имя
     * - PUT /api/users/{id} → 200 + новое имя
     * - DELETE /api/users/{id} → 204
     * - GET /api/users/{id} → 404
     */
    @Test
    void testCrudFlow() throws Exception {
        // 1) Список изначально пуст
        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // 2) Создаём пользователя alice
        UserCreateDto toCreate = new UserCreateDto();
        toCreate.setUsername("alice");
        toCreate.setPassword("Pass123");
        String jsonCreate = mapper.writeValueAsString(toCreate);

        String body = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCreate))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("alice"))
                .andReturn().getResponse().getContentAsString();

        // Извлекаем ID созданного пользователя
        UserDto created = mapper.readValue(body, UserDto.class);
        Long id = created.getId();

        // 3) GET по ID возвращает alice
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        // 4) Обновляем имя на bob
        UserUpdateDto toUpdate = new UserUpdateDto();
        toUpdate.setUsername("bob");
        String jsonUpdate = mapper.writeValueAsString(toUpdate);

        mvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));

        // 5) Удаляем пользователя
        mvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        // 6) После удаления запрос даёт 404
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}