package web.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import web.model.User;
import web.model.UserDetails;
import web.repository.UserDetailsRepository;
import web.repository.UserRepository;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"USER","ADMIN"})
class DuplicateEmailIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserDetailsRepository detailsRepo;

    @BeforeEach
    void setup() {
        detailsRepo.deleteAll();
        userRepo.deleteAll();

        // ——— Первый пользователь с деталями ———
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password");
        UserDetails det1 = new UserDetails();
        det1.setUser(user1);
        det1.setFirstName("Иван");
        det1.setLastName("Иванов");
        det1.setEmail("dup@example.com");
        user1.setDetails(det1);
        userRepo.save(user1);

        // ——— Второй пользователь без деталей ———
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password");
        // details у него null
        userRepo.save(user2);
    }

    @Test
    void whenDuplicateEmailOnDifferentUser_thenConflict() throws Exception {
        Long secondId = userRepo.findByUsername("user2").get().getId();

        String json = """
        {
          "userId": %d,
          "firstName": "Пётр",
          "lastName": "Петров",
          "email": "dup@example.com",
          "dateOfBirth": "1990-01-01",
          "phoneNumber": "+71234567890",
          "photoUrl": null
        }
        """.formatted(secondId);

        mvc.perform(post("/api/users/{id}/details", secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email уже используется")));
    }
}

