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

@Service
public class PhotoServiceImpl implements PhotoService {

    private final UserDetailsRepository detailsRepo;
    private final UserRepository userRepo;          // 1) Inject UserRepository
    private final Path rootDir;

    public PhotoServiceImpl(
            UserDetailsRepository detailsRepo,
            UserRepository userRepo,                 // ← вот здесь
            @Value("${photo.upload.dir:uploads}") String uploadDir
    ) {
        this.detailsRepo = detailsRepo;
        this.userRepo    = userRepo;               // ← и сохраняем
        this.rootDir     = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку для загрузок", e);
        }
    }

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

    @Override
    @Transactional(readOnly = true)
    public byte[] getPhoto(Long userId) {
        // читаем путь из details и сразу возвращаем байты
        var details = detailsRepo.findByUserId(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("UserDetails не найдены для userId=" + userId));

        String path = details.getPhotoUrl();
        if (path == null) {
            throw new EntityNotFoundException("Фото не загружено для userId=" + userId);
        }

        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла", e);
        }
    }

    @Override
    @Transactional
    public void uploadPhoto(Long userId, MultipartFile file) {
        // 2) вместо .orElseThrow — .orElseGet:
        UserDetails details = detailsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    // если нет деталей — создаём их
                    User user = userRepo.findById(userId)
                            .orElseThrow(() ->
                                    new EntityNotFoundException("User не найден: " + userId));
                    UserDetails newDet = new UserDetails();
                    newDet.setUser(user);
                    return newDet;
                });

        String filename = userId + "_" + file.getOriginalFilename();
        Path dest = rootDir.resolve(filename);

        try {
            Files.write(dest, file.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла", e);
        }

        details.setPhotoUrl(dest.toString());
        detailsRepo.save(details);
    }

    @Override
    @Transactional
    public void deletePhoto(Long userId) {
        UserDetails details = detailsRepo.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserDetails не найдены для userId=" + userId));

        String photoPath = details.getPhotoUrl();
        if (photoPath == null) {
            throw new EntityNotFoundException("Фото не загружено для userId=" + userId);
        }

        try {
            Files.deleteIfExists(Paths.get(photoPath));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл фото", e);
        }

        details.setPhotoUrl(null);
        detailsRepo.save(details);
    }
}