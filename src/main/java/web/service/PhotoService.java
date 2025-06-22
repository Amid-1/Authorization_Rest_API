package web.service;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {
    /**
     * Загружает фото для пользователя с заданным ID,
     * сохраняет файл на диск (или в БД) и сохраняет путь в UserDetails.photoUrl.
     */
    void uploadPhoto(Long userId, MultipartFile file);

    /**
     * Возвращает байты фото для пользователя.
     */
    byte[] getPhoto(Long userId);

    void deletePhoto(Long userId);

    String getPhotoPath(Long userId);
}
