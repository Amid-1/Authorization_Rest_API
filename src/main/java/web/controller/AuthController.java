package web.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.dto.LoginRequestDto;
import web.dto.JwtResponseDto;
import web.service.AuthService;

/**
 * Контроллер для аутентификации пользователей.
 * Обрабатывает входящие запросы по пути /api/auth.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Аутентификация по логину и паролю.
     * При успешной проверке возвращает JWT токен.
     *
     * @param request DTO с полями username и password
     * @return HTTP 200 + тело { "token": "..." }
     *         HTTP 401 при неверных учётных данных
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @RequestBody @Valid LoginRequestDto request
    ) {
        JwtResponseDto token = authService.authenticate(request);
        return ResponseEntity.ok(token);
    }
}