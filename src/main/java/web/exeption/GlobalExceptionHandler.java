package web.exeption;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 * Переопределяет ответы для различных типов ошибок:
 * - 401 при неверных учетных данных
 * - 404 при отсутствии сущности
 * - 400 при валидации
 * - 409 при нарушении целостности данных
 * - 500 для всех прочих неожиданных ошибок
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Неверный логин/пароль.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("error",   "Не авторизован");
        body.put("message", "Неверное имя пользователя или пароль");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    /**
     * Сущность не найдена в базе.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error",   "Не найдено");
        body.put("message", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    /**
     * Ошибки валидации @Valid для @RequestBody.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    /**
     * Ошибки валидации @RequestParam, @PathVariable и т.п.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                errors.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    /**
     * Нарушение целостности данных (например, уникальность).
     * Пытаемся «расшифровать» ошибку PostgreSQL.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "Нарушение целостности данных";
        Throwable root = ex.getRootCause();

        // Если это PSQLException с кодом 23505 (duplicate key)
        if (root instanceof org.postgresql.util.PSQLException pg && "23505".equals(pg.getSQLState())) {
            String detail = pg.getServerErrorMessage().getDetail();
            if (detail != null && detail.contains("(email)=")) {
                msg = "Email уже используется";
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Conflict", "message", msg));
    }

    /**
     * Общая ошибка Persistence.
     */
    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String, String>> handleConflict(PersistenceException ex) {
        String msg = "Нарушение целостности данных: "
                + Optional.ofNullable(ex.getMessage()).orElse("duplicate key");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Conflict", "message", msg));
    }

    /**
     * Любые другие неожиданные ошибки.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error",   "Внутренняя ошибка сервера");
        body.put("message", ex.getMessage() != null
                ? ex.getMessage()
                : "Неизвестная ошибка");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<Map<String,String>> handleInvalidImage(InvalidImageFormatException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error","Bad Request", "message", ex.getMessage()));
    }
}