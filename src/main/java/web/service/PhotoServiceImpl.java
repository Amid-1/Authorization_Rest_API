package web.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.model.User;
import web.model.UserDetails;
import web.repository.UserDetailsRepository;
import web.repository.UserRepository;
import web.validator.ImageValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Реализация сервиса для работы с фотографиями пользователей.
 * Файлы сохраняются на диск в директорию uploads (по умолчанию)
 * и путь хранится в UserDetails.photoUrl.
 */
@Service
public class PhotoServiceImpl implements PhotoService {

    private final ImageValidator validator;
    private final UserDetailsRepository detailsRepo;
    private final UserRepository userRepo;
    private final Path rootDir;

    /**
     * Конструктор: создаёт папку для хранения фото, если её нет.
     * @param validator    валидатор изображений — проверяет, что файл имеет поддерживаемый формат (JPEG или PNG)
     * @param detailsRepo репозиторий для UserDetails
     * @param userRepo    репозиторий для User
     * @param uploadDir   путь к директории загрузок из application.properties
     */
    public PhotoServiceImpl(
            ImageValidator validator,
            UserDetailsRepository detailsRepo,
            UserRepository userRepo,
            @Value("${photo.upload.dir:uploads}") String uploadDir
    ) {
        this.validator   = validator;
        this.detailsRepo = detailsRepo;
        this.userRepo    = userRepo;
        this.rootDir     = Paths.get(uploadDir);

        try {
            // Гарантируем, что корневая папка для изображений существует:
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для загрузок", e);
        }
    }

    @Override
    public String getPhotoMimeType(Long userId) {
        // Получаем путь к файлу (или URL), хранящийся в UserDetails
        String path = getPhotoPath(userId);
        // Определяем MIME на основании расширения или контента:
        try {
            return Files.probeContentType(Paths.get(path));
        } catch (IOException e) {
            // по умолчанию JPEG
            return MediaType.IMAGE_JPEG_VALUE;
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
     * @throws RuntimeException        если:
     *   - не удалось прочитать байты из MultipartFile,
     *   - алгоритм SHA-256 не поддерживается,
     *   - произошла ошибка при сохранении файла на диск
     */
    @Override
    @Transactional
    public void uploadPhoto(Long userId, MultipartFile file) {
        // 1) Валидация формата
        validator.checkImageType(file);

        // 2) Получаем или создаём UserDetails
        UserDetails details = detailsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    User u = userRepo.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
                    UserDetails nd = new UserDetails();
                    nd.setUser(u);
                    return nd;
                });

        // 3) Выделяем расширение
        String original = file.getOriginalFilename();
        String ext = "";
        int dot = (original != null) ? original.lastIndexOf('.') : -1;
        if (dot > 0 && original.length() > dot + 1) {
            ext = original.substring(dot); // например ".jpg"
        }

        // 4) Читаем байты и вычисляем SHA-256 хеш
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать содержимое файла", e);
        }
        String hash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-256 не поддерживается", e);
        }

        // 5) Формируем имя и записываем файл только если его ещё нет.
        //    Корневая директория гарантированно существует, поэтому здесь проверяем только файл.
        String filename = userId + "_" + hash + ext;
        Path dest = rootDir.resolve(filename);
        try {
            if (!Files.exists(dest)) {
                Files.write(dest, bytes, StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }

        // 6) Сохраняем путь в БД и выходим
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