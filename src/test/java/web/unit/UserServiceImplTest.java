package web.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import web.dto.UserCreateDto;
import web.dto.UserUpdateDto;
import web.mapper.UserMapper;
import web.model.Role;
import web.model.User;
import web.repository.RoleRepository;
import web.repository.UserRepository;
import web.service.UserServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для UserServiceImpl:
 * – проверка создания пользователя с установкой роли по умолчанию и хешированием пароля
 * – проверка обновления пароля при наличии нового значения
 */
class UserServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private RoleRepository roleRepo;
    @Mock private PasswordEncoder encoder;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_assignsDefaultRole_and_encodesPassword() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("joe");
        dto.setPassword("Secret1");
        Role userRole = new Role("ROLE_USER");
        when(roleRepo.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));
        when(encoder.encode("Secret1"))
                .thenReturn("hash");

        service.createUser(dto);

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        User saved = cap.getValue();

        assertThat(saved.getUsername()).isEqualTo("joe");
        assertThat(saved.getPassword()).isEqualTo("hash");
        assertThat(saved.getRoles()).contains(userRole);
    }

    @Test
    void updateUser_withNewPassword_encodesIt() {
        User existing = new User();
        existing.setId(10L);
        existing.setUsername("old");
        existing.setPassword("oldHash");
        when(userRepo.findById(10L))
                .thenReturn(Optional.of(existing));
        when(encoder.encode("New1"))
                .thenReturn("newHash");

        UserUpdateDto dto = new UserUpdateDto();
        dto.setUsername("old");          // username обязателен
        dto.setPassword("New1");         // новый пароль
        dto.setRoleIds(List.of());       // пустой список ролей

        service.updateUser(10L, dto);

        assertThat(existing.getPassword()).isEqualTo("newHash");
    }
}