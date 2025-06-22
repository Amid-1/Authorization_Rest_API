package web.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.model.User;
import web.model.UserDetails;
import web.repository.UserDetailsRepository;
import web.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Реализация сервиса для работы с фотографиями пользователей.
 * Файлы сохраняются на диск в директорию uploads (по умолчанию)
 * и путь хранится в UserDetails.photoUrl.
 */
@Service
public class PhotoServiceImpl implements PhotoService {

    private final UserDetailsRepository detailsRepo;
    private final UserRepository userRepo;
    private final Path rootDir;

    /**
     * Конструктор: создаёт папку для хранения фото, если её нет.
     *
     * @param detailsRepo репозиторий для UserDetails
     * @param userRepo    репозиторий для User
     * @param uploadDir   путь к директории загрузок из application.properties
     */
    public PhotoServiceImpl(
            UserDetailsRepository detailsRepo,
            UserRepository userRepo,
            @Value("${photo.upload.dir:uploads}") String uploadDir
    ) {
        this.detailsRepo = detailsRepo;
        this.userRepo    = userRepo;
        this.rootDir     = Paths.get(uploadDir);

        try {
            // Создаём каталог для хранения изображений
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для загрузок", e);
        }
    }

    /**
     * Возвращает путь к файлу фото из UserDetails.photoUrl.
     *
     * @param userId ID пользователя
     * @return абсолютный путь к файлу
     * @throws EntityNotFoundException если детали или путь не найдены
     */
    @Override
    public String getPhotoPath(Long userId) {
        var details = detailsRepo.findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("UserDetails не найдены для userId=" + userId));

        String path = details.getPhotoUrl();
        if (path == null) {
            throw new EntityNotFoundException("Фото не загружено для userId=" + userId);
        }
        return path;
    }

    /**
     * Читает и возвращает байты файла фото.
     *
     * @param userId ID пользователя
     * @return массив байт изображения
     * @throws EntityNotFoundException если фото не найдены
     * @throws RuntimeException        если ошибка чтения файла
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] getPhoto(Long userId) {
        var details = detailsRepo.findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("UserDetails не найдены для userId=" + userId));

        String path = details.getPhotoUrl();
        if (path == null) {
            throw new EntityNotFoundException("Фото не загружено для userId=" + userId);
        }

        try {
            // Считываем файл целиком
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла", e);
        }
    }

    /**
     * Загружает фото: сохраняет файл на диск и обновляет UserDetails.photoUrl.
     *
     * @param userId ID пользователя
     * @param file   multipart-файл с изображением
     * @throws EntityNotFoundException если пользователь не найден
     * @throws RuntimeException        при ошибках записи файла
     */
    @Override
    @Transactional
    public void uploadPhoto(Long userId, MultipartFile file) {
        // Получаем или создаём запись UserDetails
        UserDetails details = detailsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepo.findById(userId)
                            .orElseThrow(() ->
                                    new EntityNotFoundException("User не найден: " + userId));
                    UserDetails newDet = new UserDetails();
                    newDet.setUser(user);
                    return newDet;
                });

        // Формируем уникальное имя файла
        String filename = userId + "_" + file.getOriginalFilename();
        Path dest = rootDir.resolve(filename);

        try {
            // Записываем байты на диск, перезаписывая существующий
            Files.write(dest, file.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }

        // Сохраняем путь в базу
        details.setPhotoUrl(dest.toString());
        detailsRepo.save(details);
    }

    /**
     * Удаляет файл фото и очищает поле photoUrl.
     *
     * @param userId ID пользователя
     * @throws EntityNotFoundException если фото не найдены
     */
    @Override
    @Transactional
    public void deletePhoto(Long userId) {
        UserDetails details = detailsRepo.findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("UserDetails не найдены для userId=" + userId));

        String photoPath = details.getPhotoUrl();
        if (photoPath == null) {
            throw new EntityNotFoundException("Фото не загружено для userId=" + userId);
        }

        try {
            // Удаляем файл, если он существует
            Files.deleteIfExists(Paths.get(photoPath));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл фото", e);
        }

        // Очищаем ссылку в базе
        details.setPhotoUrl(null);
        detailsRepo.save(details);
    }
}