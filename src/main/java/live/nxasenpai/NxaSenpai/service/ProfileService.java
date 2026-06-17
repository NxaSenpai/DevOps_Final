package live.nxasenpai.NxaSenpai.service;

import live.nxasenpai.NxaSenpai.model.Profile;
import live.nxasenpai.NxaSenpai.model.ProfileType;
import live.nxasenpai.NxaSenpai.repository.ProfileRepository;
import live.nxasenpai.NxaSenpai.util.PhotoValidator;
import live.nxasenpai.NxaSenpai.util.RegistrationNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final RegistrationNumberGenerator regNumberGenerator;
    private final PhotoValidator photoValidator;

    private static final String UPLOAD_DIR = "uploads/photos/";

    public ProfileService(ProfileRepository profileRepository,
                          RegistrationNumberGenerator regNumberGenerator,
                          PhotoValidator photoValidator) {
        this.profileRepository = profileRepository;
        this.regNumberGenerator = regNumberGenerator;
        this.photoValidator = photoValidator;
    }

    // ── CRUD ──

    public Profile createProfile(Profile profile) {
        if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().isBlank()) {
            profile.setRegistrationNumber(
                    regNumberGenerator.generate(profile.getDepartment()));
        }
        return profileRepository.save(profile);
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Optional<Profile> getProfileById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> getProfileByRegistrationNumber(String regNumber) {
        return profileRepository.findByRegistrationNumber(regNumber);
    }

    public Profile updateProfile(Long id, Profile updated) {
        Profile existing = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        existing.setProfileType(updated.getProfileType());
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setDepartment(updated.getDepartment());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setAddress(updated.getAddress());

        // Only update photo path if a new one is provided
        if (updated.getPhotoPath() != null && !updated.getPhotoPath().isBlank()) {
            existing.setPhotoPath(updated.getPhotoPath());
        }

        return profileRepository.save(existing);
    }

    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("Profile not found with id: " + id);
        }
        profileRepository.deleteById(id);
    }

    // ── Photo Upload ──

    public String uploadPhoto(Long profileId, MultipartFile file) throws IOException {
        String error = photoValidator.validate(file);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + profileId));

        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String storedFilename = UUID.randomUUID() + ext;

        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        profile.setPhotoPath("/photos/" + storedFilename);
        profileRepository.save(profile);

        return profile.getPhotoPath();
    }

    // ── Search & Filter ──

    public List<Profile> searchProfiles(String query) {
        return profileRepository.search(query);
    }

    public List<Profile> getProfilesByType(ProfileType profileType) {
        return profileRepository.findByProfileType(profileType);
    }

    public List<Profile> getProfilesByDepartment(String department) {
        return profileRepository.findByDepartment(department);
    }

    // ── Batch ──

    public List<Profile> createBatch(List<Profile> profiles) {
        for (Profile profile : profiles) {
            if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().isBlank()) {
                profile.setRegistrationNumber(
                        regNumberGenerator.generate(profile.getDepartment()));
            }
        }
        return profileRepository.saveAll(profiles);
    }

    public long countProfiles() {
        return profileRepository.count();
    }
}
