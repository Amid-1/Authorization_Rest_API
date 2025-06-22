package web.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import web.dto.LoginRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для AuthController.
 * Проверяют, что при корректных и некорректных
 * учётных данных `/api/auth/login` возвращает
 * соответствующие HTTP-статусы и тело ответа.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    /**
     * Должен вернуть HTTP 200 и непустой токен
     * при правильных admin:admin.
     */
    @Test
    void loginSuccess() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername("admin");
        req.setPassword("admin");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * Должен вернуть HTTP 401 при неверном пароле.
     */
    @Test
    void loginFailure() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername("admin");
        req.setPassword("wrong");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}