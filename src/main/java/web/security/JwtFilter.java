package web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import web.service.CustomUserDetailsService;
import web.util.JwtUtil;
import java.io.IOException;

/**
 * Фильтр, который перехватывает каждый HTTP-запрос,
 * извлекает JWT из заголовка Authorization,
 * валидирует его и, если всё хорошо, формирует Authentication
 * и сохраняет в SecurityContext.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Извлекаем заголовок Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        // Ожидаем формат: "Bearer <token>"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Если токен есть и он валиден, получаем из него username
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);

            // Загружаем детали пользователя по username
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Создаём объект аутентификации
            var auth = new org.springframework.security.authentication
                    .UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            // Привязываем детали запроса (IP, сессия)
            auth.setDetails(new WebAuthenticationDetailsSource()
                    .buildDetails(request));

            // Сохраняем аутентификацию в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}