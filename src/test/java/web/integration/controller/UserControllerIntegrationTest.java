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

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"USER","ADMIN"})
class UserControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository userRepo;

    @BeforeEach
    void clean() {
        userRepo.deleteAll();
    }

    @Test
    void testCrudFlow() throws Exception {
        // 1) GET пустого списка
        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // 2) POST → создать (UserCreateDto)
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

        // Ответ мапим в UserDto (без пароля)
        UserDto created = mapper.readValue(body, UserDto.class);
        Long id = created.getId();

        // 3) GET /{id}
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        // 4) PUT → изменить username (UserUpdateDto)
        UserUpdateDto toUpdate = new UserUpdateDto();
        toUpdate.setUsername("bob");
        // пароль не устанавливаем — он останется прежним
        String jsonUpdate = mapper.writeValueAsString(toUpdate);

        mvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));

        // 5) DELETE → удалить
        mvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        // 6) GET того же → 404
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
