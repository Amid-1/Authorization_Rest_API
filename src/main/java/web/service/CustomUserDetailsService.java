package web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import web.repository.UserRepository;
import web.model.User;
import java.util.stream.Collectors;

/**
 * Сервис загрузки данных пользователя для Spring Security.
 * Реализует UserDetailsService: находит пользователя по username
 * и преобразует его роли в GrantedAuthority.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает UserDetails по имени пользователя.
     * Выбрасывает UsernameNotFoundException, если пользователь не найден.
     *
     * @param username логин пользователя
     * @return объект UserDetails с username, password и ролями
     * @throws UsernameNotFoundException если в базе нет пользователя с таким логином
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ищем в базе пользователя по username
        User appUser = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User with username=" + username + " not found")
                );

        // Конвертируем роли в список SimpleGrantedAuthority
        var authorities = appUser.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());

        // Возвращаем стандартную реализацию UserDetails
        return new org.springframework.security.core.userdetails.User(
                appUser.getUsername(),
                appUser.getPassword(),
                authorities
        );
    }
}