package web.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import web.model.Role;
import web.model.User;
import web.repository.RoleRepository;
import web.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

/**
 * Компонент, который при старте приложения проверяет инициализацию
 * ролей и администратора. Если их нет в БД, создаёт:
 * - роль ROLE_USER
 * - роль ROLE_ADMIN
 * - пользователя "admin" с паролем "admin" и обеими ролями
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository   roleRepo;
    private final UserRepository   userRepo;
    private final PasswordEncoder  passwordEncoder;

    public DataInitializer(RoleRepository roleRepo,
                           UserRepository userRepo,
                           PasswordEncoder passwordEncoder) {
        this.roleRepo        = roleRepo;
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Запускается автоматически после старта контекста Spring.
     * В рамках одной транзакции:
     * 1) Ищет или создаёт роли ROLE_USER и ROLE_ADMIN.
     * 2) Если нет пользователя с логином "admin", создаёт его с паролем "admin" (захешированным) и обеими ролями.
     *
     * @param args аргументы запуска (не используются)
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Проверяем и создаём роль ROLE_USER, если необходимо
        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_USER")));

        // Проверяем и создаём роль ROLE_ADMIN, если необходимо
        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

        String adminLogin = "admin";
        // Если администратора ещё нет, создаём его
        if (userRepo.findByUsername(adminLogin).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminLogin);
            // Хешируем пароль перед сохранением
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRoles(Set.of(adminRole, userRole));
            userRepo.save(admin);
        }
    }
}