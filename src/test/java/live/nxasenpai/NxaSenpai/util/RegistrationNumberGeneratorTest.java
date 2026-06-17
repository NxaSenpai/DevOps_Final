package live.nxasenpai.NxaSenpai.util;

import live.nxasenpai.NxaSenpai.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationNumberGeneratorTest {

    @Mock
    private ProfileRepository profileRepository;

    @Test
    void generate_ShouldCreateExpectedFormat() {
        when(profileRepository.existsByRegistrationNumber(anyString())).thenReturn(false);

        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        String regNumber = generator.generate("Computer Science");

        // Format: YEAR-CS-####
        assertNotNull(regNumber);
        assertTrue(regNumber.matches("\\d{4}-CS-\\d{4}"),
                "Expected format YEAR-CS-####, got: " + regNumber);
    }

    @Test
    void generate_WithMultiWordDepartment_ShouldUseInitials() {
        when(profileRepository.existsByRegistrationNumber(anyString())).thenReturn(false);

        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        String regNumber = generator.generate("Electrical Engineering");

        assertNotNull(regNumber);
        assertTrue(regNumber.matches("\\d{4}-EE-\\d{4}"),
                "Expected format YEAR-EE-####, got: " + regNumber);
    }

    @Test
    void generate_WithNullDepartment_ShouldUseGEN() {
        when(profileRepository.existsByRegistrationNumber(anyString())).thenReturn(false);

        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        String regNumber = generator.generate(null);

        assertNotNull(regNumber);
        assertTrue(regNumber.matches("\\d{4}-GEN-\\d{4}"),
                "Expected format YEAR-GEN-####, got: " + regNumber);
    }

    @Test
    void generateUuid_ShouldReturn8CharString() {
        when(profileRepository.existsByRegistrationNumber(anyString())).thenReturn(false);

        RegistrationNumberGenerator generator = new RegistrationNumberGenerator(profileRepository);
        String uuid = generator.generateUuid();

        assertNotNull(uuid);
        assertEquals(8, uuid.length());
        assertEquals(uuid, uuid.toUpperCase());
    }
}
