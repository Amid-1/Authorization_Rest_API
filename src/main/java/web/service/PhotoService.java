package web.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис для работы с фотографиями пользователей:
 * загрузка, получение, удаление и метаданные (путь и MIME-тип).
 */
public interface PhotoService {

    /**
     * Загружает фото для пользователя с заданным ID.
     * Сохраняет файл на диск (или в БД) и сохраняет путь в UserDetails.photoUrl.
     *
     * @param userId ID пользователя
     * @param file   multipart-файл с изображением
     * @throws EntityNotFoundException если пользователь не найден
     * @throws RuntimeException        при ошибках чтения или сохранения файла
     */
    void uploadPhoto(Long userId, MultipartFile file);

    /**
     * Возвращает байты ранее загруженного фото пользователя.
     *
     * @param userId ID пользователя
     * @return массив байт изображения
     * @throws EntityNotFoundException если фото не загружено
     */
    byte[] getPhoto(Long userId);

    /**
     * Удаляет файл фото и очищает поле UserDetails.photoUrl.
     *
     * @param userId ID пользователя
     * @throws EntityNotFoundException если фото не загружено
     */
    void deletePhoto(Long userId);

    /**
     * Возвращает абсолютный путь к файлу фото из UserDetails.photoUrl.
     *
     * @param userId ID пользователя
     * @return путь к файлу
     * @throws EntityNotFoundException если фото не загружено
     */
    String getPhotoPath(Long userId);

    /**
     * Определяет MIME-тип загруженного фото (например, image/jpeg).
     *
     * @param userId ID пользователя
     * @return строка-значение MIME-типа
     * @throws EntityNotFoundException если фото не загружено
     */
    String getPhotoMimeType(Long userId);
}
