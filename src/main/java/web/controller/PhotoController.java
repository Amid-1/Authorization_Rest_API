package web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import web.service.PhotoService;

@RestController
public class PhotoController implements PhotoApi {
    private final PhotoService svc;
    public PhotoController(PhotoService svc) { this.svc = svc; }

    @Override
    public ResponseEntity<Void> upload(Long userId, MultipartFile file) {
        svc.uploadPhoto(userId, file);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<byte[]> download(Long userId) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(svc.getPhotoMimeType(userId)))
                .body(svc.getPhoto(userId));
    }

    @Override
    public ResponseEntity<Void> delete(Long userId) {
        svc.deletePhoto(userId);
        return ResponseEntity.noContent().build();
    }
}
