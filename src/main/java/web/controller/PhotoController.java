package web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import web.service.PhotoService;

import java.io.IOException;

/**
 * REST-контроллер для работы с фотографией пользователя:
 * загрузка, скачивание и удаление.
 * Базовый путь: /api/users/{userId}/photo
 */
@RestController
@RequestMapping("/api/users/{userId}/photo")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * Загружает фотографию для пользователя с заданным ID.
     * Поддерживаются только JPEG и PNG.
     *
     * @param userId ID пользователя
     * @param file   multipart-файл с изображением
     * @return 200 OK при успешной загрузке, 400 Bad Request при неподдерживаемом формате
     * @throws IOException если не удалось прочитать содержимое файла
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        // Проверяем, что прислали изображение нужного формата
        String ct = file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equals(ct) &&
                !MediaType.IMAGE_PNG_VALUE.equals(ct)) {
            return ResponseEntity.badRequest().build();
        }

        photoService.uploadPhoto(userId, file);
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает ранее загруженное фото пользователя в виде массива байт.
     *
     * @param userId ID пользователя
     * @return 200 OK + изображение (Content-Type: image/jpeg),
     *         404 Not Found, если фото не загружено
     */
    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable Long userId) {
        byte[] img = photoService.getPhoto(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // можно динамически ставить PNG, если храните тип
                .body(img);
    }

    /**
     * Удаляет фотографию пользователя.
     *
     * @param userId ID пользователя
     * @return 204 No Content при успешном удалении,
     *         404 Not Found, если фото не было загружено
     */
    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        photoService.deletePhoto(userId);
        return ResponseEntity.noContent().build();
    }
}