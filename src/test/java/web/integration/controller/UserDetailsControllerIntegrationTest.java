package web.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест для UserDetailsController.
 * Проверяет валидацию поля phoneNumber при сохранении деталей пользователя.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"USER","ADMIN"})
class UserDetailsControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    /**
     * Отправляем POST /api/users/999/details с некорректным номером телефона.
     * Ожидаем HTTP 400 и сообщение об ошибке по полю phoneNumber.
     */
    @Test
    void whenInvalidPhone_thenBadRequest() throws Exception {
        String json = """
            {
              "userId": 999,
              "firstName": "Иван",
              "lastName": "Иванов",
              "email": "ivan@example.com",
              "dateOfBirth": "1990-01-01",
              "phoneNumber": "12345",
              "photoUrl": null
            }
            """;

        mvc.perform(post("/api/users/999/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // Ожидаем ошибку валидации поля phoneNumber
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber", containsString("должен соответствовать")));
    }
}