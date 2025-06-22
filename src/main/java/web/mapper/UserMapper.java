package web.mapper;

import org.springframework.stereotype.Component;
import web.dto.UserCreateDto;
import web.dto.UserDto;
import web.dto.UserUpdateDto;
import web.model.Role;
import web.model.User;
import web.repository.RoleRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    private final RoleRepository roleRepository;

    public UserMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Для ответа клиенту.
     */
    public UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoleIds(
                user.getRoles().stream()
                        .map(Role::getId)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    /**
     * Для создания нового User из UserCreateDto.
     */
    public User toEntity(UserCreateDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        // роли будут установлены в сервисе; здесь можно скопировать, если dto.getRoleIds() != null
        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            user.setRoles(roles);
        }
        return user;
    }

    /**
     * Для обновления существующего User из UserUpdateDto.
     */
    public void updateEntity(UserUpdateDto dto, User existing) {
        existing.setUsername(dto.getUsername());
        if (dto.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(dto.getRoleIds()));
            existing.setRoles(roles);
        }
    }
}