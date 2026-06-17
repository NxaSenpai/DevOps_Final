package live.nxasenpai.NxaSenpai.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates photo uploads for allowed types (JPEG, PNG) and file size.
 */
@Component
public class PhotoValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png"
    ));

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png"
    ));

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    public boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }

        if (originalFilename != null) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            return ALLOWED_EXTENSIONS.contains(ext);
        }

        return false;
    }

    public boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE_BYTES;
    }

    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is empty or missing.";
        }
        if (!isValidFileType(file)) {
            return "Invalid file type. Only JPEG and PNG images are allowed.";
        }
        if (!isValidFileSize(file)) {
            return "File size exceeds the 5 MB limit.";
        }
        return null; // null means valid
    }
}
