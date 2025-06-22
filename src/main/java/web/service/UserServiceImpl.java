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

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("User with id=" + id + " not found"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // — mutable Set вместо immutable
        Set<Role> roles = new HashSet<>();
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new EntityNotFoundException("Default role not found"));
            roles.add(defaultRole);
        } else {
            // извлекаем роли и собираем в HashSet
            roleRepository.findAllById(dto.getRoleIds())
                    .forEach(roles::add);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("User with id=" + id + " not found"));

        // 1) username
        existing.setUsername(dto.getUsername());

        // 2) роли
        if (dto.getRoleIds() != null) {
            // вариант А: полностью заменить на новый mutable Set
            Set<Role> newRoles = dto.getRoleIds().stream()
                    .map(roleRepository::getById)      // getById сразу бросит, если нет
                    .collect(Collectors.toSet());      // HashSet под капотом
            existing.setRoles(newRoles);

            // вариант Б (если хотите не менять сам объект-сет, а чистить/добавлять):
            // existing.getRoles().clear();
            // existing.getRoles().addAll(newRoles);
        }

        // 3) пароль, если пришёл
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User saved = userRepository.save(existing);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id=" + id + " not found");
        }
        userRepository.deleteById(id);
    }
}