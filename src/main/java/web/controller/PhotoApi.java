package web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/users/{userId}/photo")
public interface PhotoApi {
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> upload(@PathVariable Long userId,
                                @RequestPart("file") MultipartFile file);

    @GetMapping
    ResponseEntity<byte[]> download(@PathVariable Long userId);

    @DeleteMapping
    ResponseEntity<Void> delete(@PathVariable Long userId);
}
