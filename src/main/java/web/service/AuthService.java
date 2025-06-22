package web.service;

import web.dto.LoginRequestDto;
import web.dto.JwtResponseDto;

public interface AuthService {
    /**
     * Проверяет логин/пароль, генерирует JWT и возвращает его в обёртке.
     */
    JwtResponseDto authenticate(LoginRequestDto request);
}
