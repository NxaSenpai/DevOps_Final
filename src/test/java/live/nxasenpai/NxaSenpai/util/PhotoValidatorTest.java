package live.nxasenpai.NxaSenpai.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class PhotoValidatorTest {

    private final PhotoValidator validator = new PhotoValidator();

    @Test
    void validate_ValidJpeg_ShouldReturnNull() {
        MultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", new byte[1000]);

        assertNull(validator.validate(file));
    }

    @Test
    void validate_ValidPng_ShouldReturnNull() {
        MultipartFile file = new MockMultipartFile(
                "photo", "test.png", "image/png", new byte[1000]);

        assertNull(validator.validate(file));
    }

    @Test
    void validate_NullFile_ShouldReturnError() {
        String error = validator.validate(null);
        assertNotNull(error);
        assertTrue(error.contains("empty") || error.contains("missing"));
    }

    @Test
    void validate_InvalidFileType_ShouldReturnError() {
        MultipartFile file = new MockMultipartFile(
                "photo", "test.gif", "image/gif", new byte[1000]);

        String error = validator.validate(file);
        assertNotNull(error);
        assertTrue(error.contains("Invalid file type"));
    }

    @Test
    void validate_FileTooLarge_ShouldReturnError() {
        MultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", new byte[6 * 1024 * 1024]);

        String error = validator.validate(file);
        assertNotNull(error);
        assertTrue(error.contains("size"));
    }

    @Test
    void isValidFileType_JpegExtension_ShouldBeValid() {
        MultipartFile file = new MockMultipartFile(
                "photo", "photo.jpeg", null, new byte[100]);
        assertTrue(validator.isValidFileType(file));
    }

    @Test
    void isValidFileSize_WithinLimit_ShouldBeValid() {
        MultipartFile file = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", new byte[1024]);
        assertTrue(validator.isValidFileSize(file));
    }
}
