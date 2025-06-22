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

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 401 — неправильные креденшалы
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("error",   "Не авторизован");
        body.put("message", "Неверное имя пользователя или пароль");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    // 404 — когда не нашли сущность
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Не найдено");
        body.put("message", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    // 400 — когда @Valid не прошёл в контроллере
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    // 400 — ошибки в @RequestParam, @PathVariable и т.п.
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "Нарушение целостности данных";
        Throwable root = ex.getRootCause();

        if (root instanceof org.postgresql.util.PSQLException) {
            org.postgresql.util.PSQLException pg = (org.postgresql.util.PSQLException) root;
            // SQLState 23505 — уникальность нарушена
            if ("23505".equals(pg.getSQLState())) {
                String detail = pg.getServerErrorMessage().getDetail();
                // detail: «Ключ "(email)=(dup@example.com)" уже существует.»
                if (detail != null && detail.contains("(email)=")) {
                    msg = "Email уже используется";
                }
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error",   "Conflict",
                        "message", msg
                ));
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String,String>> handleConflict(PersistenceException ex) {
        String msg = "Нарушение целостности данных: "
                + Optional.ofNullable(ex.getMessage()).orElse("duplicate key");
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Conflict", "message", msg));
    }

    // 500 — все остальные неожиданные ошибки
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
}
