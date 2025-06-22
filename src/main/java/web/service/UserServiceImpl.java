package web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.dto.UserCreateDto;
import web.dto.UserDto;
import web.dto.UserUpdateDto;
import web.mapper.UserMapper;
import web.model.Role;
import web.model.User;
import web.repository.RoleRepository;
import web.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления пользователями:
 * создание, чтение, обновление и удаление (CRUD).
 */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository   userRepository;
    private final RoleRepository   roleRepository;
    private final PasswordEncoder  passwordEncoder;
    private final UserMapper       userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper      = userMapper;
    }

    /**
     * Возвращает всех пользователей в виде DTO.
     */
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Находит пользователя по ID.
     *
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("User with id=" + id + " not found")
                );
        return userMapper.toDto(user);
    }

    /**
     * Создаёт пользователя с обязательным хешированием пароля
     * и установкой ролей (по умолчанию ROLE_USER).
     */
    @Override
    @Transactional
    public UserDto createUser(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Если роли не передали, ставим роль по умолчанию
        Set<Role> roles = new HashSet<>();
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new EntityNotFoundException("Default role not found"));
            roles.add(defaultRole);
        } else {
            roleRepository.findAllById(dto.getRoleIds())
                    .forEach(roles::add);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    /**
     * Обновляет пользователя: имя, роли и (опционально) пароль.
     */
    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("User with id=" + id + " not found")
                );

        existing.setUsername(dto.getUsername());

        if (dto.getRoleIds() != null) {
            Set<Role> newRoles = dto.getRoleIds().stream()
                    .map(roleRepository::getById)
                    .collect(Collectors.toSet());
            existing.setRoles(newRoles);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(existing);
        return userMapper.toDto(saved);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @throws EntityNotFoundException если пользователь не существует
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id=" + id + " not found");
        }
        userRepository.deleteById(id);
    }
}