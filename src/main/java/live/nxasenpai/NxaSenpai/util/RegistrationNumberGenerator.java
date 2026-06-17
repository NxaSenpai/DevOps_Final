package live.nxasenpai.NxaSenpai.util;

import live.nxasenpai.NxaSenpai.repository.ProfileRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique registration numbers in the format: YEAR-DEPT-###
 * Example: 2026-CS-001
 */
@Component
public class RegistrationNumberGenerator {

    private final ProfileRepository profileRepository;

    public RegistrationNumberGenerator(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Generates a unique registration number with the given department code.
     */
    public String generate(String departmentCode) {
        String dept = normalizeDepartment(departmentCode);
        String prefix = Year.now() + "-" + dept + "-";
        String regNumber;
        int attempts = 0;
        do {
            int seq = ThreadLocalRandom.current().nextInt(1, 10000);
            regNumber = prefix + String.format("%04d", seq);
            attempts++;
        } while (profileRepository.existsByRegistrationNumber(regNumber) && attempts < 50);

        if (profileRepository.existsByRegistrationNumber(regNumber)) {
            // fallback: use timestamp-based suffix
            regNumber = prefix + System.currentTimeMillis() % 100000;
        }
        return regNumber;
    }

    /**
     * Generates a unique registration number using UUID (shortened).
     */
    public String generateUuid() {
        String uuid;
        do {
            uuid = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (profileRepository.existsByRegistrationNumber(uuid));
        return uuid;
    }

    /**
     * Normalizes a department name into a short uppercase code.
     * E.g., "Computer Science" → "CS", "Electrical Engineering" → "EE"
     */
    private String normalizeDepartment(String department) {
        if (department == null || department.isBlank()) {
            return "GEN";
        }
        String[] words = department.trim().split("\\s+");
        StringBuilder code = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                code.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        String result = code.toString();
        if (result.length() > 4) {
            result = result.substring(0, 4);
        }
        return result.toUpperCase();
    }
}
