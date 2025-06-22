package web.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import web.dto.LoginRequestDto;
import web.dto.JwtResponseDto;
import web.util.JwtUtil;

/**
 * Сервис аутентификации пользователей.
 * Принимает логин и пароль, проверяет их через AuthenticationManager
 * и выдаёт JWT токен.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthenticationManager authManager, JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Аутентифицирует пользователя по логину и паролю.
     * В случае успеха генерирует JWT по username и возвращает его в DTO.
     *
     * @param request DTO с полями username и password
     * @return JwtResponseDto с полем token
     * @throws org.springframework.security.core.AuthenticationException
     *         при неверных учётных данных
     */
    @Override
    public JwtResponseDto authenticate(LoginRequestDto request) {
        // Пытаемся аутентифицировать пользователя
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Генерируем JWT для пройденной аутентификации
        String token = jwtUtil.generateToken(authentication.getName());
        return new JwtResponseDto(token);
    }
}