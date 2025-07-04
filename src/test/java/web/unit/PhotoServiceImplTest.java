package web.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import web.model.UserDetails;
import web.repository.UserDetailsRepository;
import web.repository.UserRepository;
import web.service.PhotoServiceImpl;

import jakarta.persistence.EntityNotFoundException;
import web.validator.ImageValidator;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для PhotoServiceImpl:
 * – проверка сохранения файла и обновления поля photoUrl
 * – поведение при отсутствии UserDetails (EntityNotFoundException)
 */
class PhotoServiceImplTest {

    @Mock
    private ImageValidator validator;

    @Mock
    private UserRepository userRepo;

    @Mock private UserDetailsRepository detailsRepo;
    private PhotoServiceImpl service;

    @TempDir Path tempDir;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new PhotoServiceImpl(validator, detailsRepo, userRepo, tempDir.toString());
    }

    @Test
    void uploadPhoto_savesFile_and_updatesUrl() throws Exception {
        // подготовка
        UserDetails det = new UserDetails();
        det.setUser(new web.model.User());
        when(detailsRepo.findByUserId(1L)).thenReturn(Optional.of(det));

        byte[] data = "hello".getBytes();
        MockMultipartFile mf = new MockMultipartFile("file", "f.txt",
                "text/plain", data);

        // вызываем
        service.uploadPhoto(1L, mf);

        // проверяем, что details.photoUrl не пустой и файл там лежит
        String photoUrl = det.getPhotoUrl();
        assertThat(photoUrl).isNotNull();

        Path written = Path.of(photoUrl);
        // файл должен лежать именно в нашей tempDir
        assertThat(written.getParent()).isEqualTo(tempDir);
        // и файл реально создан
        assertThat(written).exists();

        // и репозиторий был сохранён
        verify(detailsRepo).save(det);
    }

    @Test
    void getPhoto_noDetails_throws() {
        when(detailsRepo.findByUserId(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPhoto(2L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
