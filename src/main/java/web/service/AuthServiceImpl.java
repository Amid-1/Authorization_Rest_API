package web.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import web.dto.LoginRequestDto;
import web.dto.JwtResponseDto;
import web.util.JwtUtil;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthenticationManager authManager, JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public JwtResponseDto authenticate(LoginRequestDto request) {
        // пробуем аутентифицировать пользователя
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // если аутентификация прошла успешно, сгенерим токен
        String token = jwtUtil.generateToken(authentication.getName());
        return new JwtResponseDto(token);
    }
}