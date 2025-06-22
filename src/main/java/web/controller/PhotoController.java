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

@RestController
@RequestMapping("/api/users/{userId}/photo")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String ct = file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equals(ct) &&
                !MediaType.IMAGE_PNG_VALUE.equals(ct)) {
            return ResponseEntity.badRequest().build();
        }
        photoService.uploadPhoto(userId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<byte[]> download(@PathVariable Long userId) {
        byte[] img = photoService.getPhoto(userId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(img);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long userId) {
        photoService.deletePhoto(userId);
        return ResponseEntity.noContent().build();
    }
}