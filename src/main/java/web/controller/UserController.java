package web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import web.dto.UserCreateDto;
import web.dto.UserDto;
import web.dto.UserUpdateDto;
import web.service.UserService;
import java.util.List;

/**
 * REST-контроллер для управления пользователями.
 * Базовый URI: /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return список UserDto
     */
    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAllUsers();
    }

    /**
     * Возвращает одного пользователя по его ID.
     *
     * @param id ID пользователя
     * @return UserDto найденного пользователя
     */
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * Создаёт нового пользователя.
     *
     * @param dto данные для создания (username, password, роли)
     * @return ResponseEntity с телом созданного UserDto и кодом 201
     */
    @PostMapping
    public ResponseEntity<UserDto> create(
            @RequestBody @Valid UserCreateDto dto) {
        UserDto created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновляет информацию о пользователе.
     *
     * @param id  ID пользователя
     * @param dto новые данные для пользователя
     * @return обновлённый UserDto
     */
    @PutMapping("/{id}")
    public UserDto update(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDto dto) {
        return userService.updateUser(id, dto);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param id ID пользователя
     * @return ResponseEntity с кодом 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}