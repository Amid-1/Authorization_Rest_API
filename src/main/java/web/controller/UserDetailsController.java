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
import web.dto.UserDetailsDto;
import web.service.UserDetailsService;

/**
 * REST-контроллер для управления детальной информацией пользователя.
 * Базовый путь: /api/users/{userId}/details
 */
@RestController
@RequestMapping("/api/users/{userId}/details")
public class UserDetailsController {

    private final UserDetailsService detailsService;

    public UserDetailsController(UserDetailsService detailsService) {
        this.detailsService = detailsService;
    }

    /**
     * Получить детали пользователя.
     *
     * @param userId ID пользователя
     * @return UserDetailsDto с данными (имя, email, телефон и т.д.)
     */
    @GetMapping
    public UserDetailsDto get(@PathVariable Long userId) {
        return detailsService.getDetails(userId);
    }

    /**
     * Создать или обновить детали пользователя.
     * Использует тот же сервисный метод для POST и PUT.
     *
     * @param userId ID пользователя
     * @param dto    данные деталей (firstName, lastName, email и т.д.)
     * @return ResponseEntity с созданным/обновлённым DTO и статусом 201
     */
    @PostMapping
    public ResponseEntity<UserDetailsDto> create(
            @PathVariable Long userId,
            @RequestBody @Valid UserDetailsDto dto
    ) {
        UserDetailsDto created = detailsService.createOrUpdate(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить детали пользователя.
     *
     * @param userId ID пользователя
     * @param dto    новые данные деталей
     * @return обновлённый UserDetailsDto
     */
    @PutMapping
    public UserDetailsDto update(
            @PathVariable Long userId,
            @RequestBody @Valid UserDetailsDto dto
    ) {
        return detailsService.createOrUpdate(userId, dto);
    }

    /**
     * Удалить детали пользователя.
     *
     * @param userId ID пользователя
     * @return ResponseEntity с кодом 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        detailsService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
