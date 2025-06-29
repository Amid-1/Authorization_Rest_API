package web.validator;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import web.exeption.InvalidImageFormatException;

import java.util.List;

@Component
public class ImageValidator {

    private static final List<String> ALLOWED = List.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    public void checkImageType(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || ALLOWED.stream().noneMatch(ct::equals)) {
            throw new InvalidImageFormatException(
                    "Unsupported image type: " + ct + ". Only JPEG and PNG are allowed."
            );
        }
    }
}
