package web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import web.service.CustomUserDetailsService;

/**
 * Конфигурация Spring Security:
 * 1. Отключает CSRF (для REST API).
 * 2. Устанавливает stateless-сессию (JWT).
 * 3. Разрешает открытый доступ к /api/auth/** и требует аутентификации для остальных запросов.
 * 4. Регистрирует кастомный UserDetailsService и JWT-фильтр.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailsService uds) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = uds;
    }

    /**
     * Позволяет автоматически внедрять AuthenticationManager в сервисы.
     *
     * @param authConfig конфигурация аутентификации Spring
     * @return AuthenticationManager
     * @throws Exception при ошибках получения менеджера
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Основная цепочка фильтров безопасности:
     * - CSRF отключён (REST).
     * - Сессии не используются (JWT stateless).
     * - Все запросы кроме /api/auth/** требуют аутентификации.
     * - Кастомный UserDetailsService и JwtFilter.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем защиту от CSRF, так как используем JWT
                .csrf(csrf -> csrf.disable())
                // Делаем приложение stateless
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Конфигурируем права доступа
                .authorizeHttpRequests(authz -> authz
                        // Разрешаем все на /api/auth/**
                        .requestMatchers("/api/auth/**").permitAll()
                        // Остальные эндпоинты — только для аутентифицированных
                        .anyRequest().authenticated()
                )
                // Внедряем наш UserDetailsService
                .userDetailsService(userDetailsService)
                // Регистрируем JWT-фильтр до стандартного UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Бин для хеширования паролей BCrypt.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}