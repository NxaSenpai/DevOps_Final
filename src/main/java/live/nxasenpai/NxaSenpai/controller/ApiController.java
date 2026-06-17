package live.nxasenpai.NxaSenpai.controller;

import live.nxasenpai.NxaSenpai.model.Profile;
import live.nxasenpai.NxaSenpai.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for programmatic access.
 * Base path: /api
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ProfileService profileService;

    public ApiController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // ── Profile CRUD ──

    @GetMapping("/profiles")
    public List<Profile> listProfiles() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<Profile> getProfile(@PathVariable Long id) {
        return profileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/profiles")
    public ResponseEntity<Profile> createProfile(@Valid @RequestBody Profile profile) {
        Profile created = profileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/profiles/{id}")
    public ResponseEntity<Profile> updateProfile(@PathVariable Long id,
                                                  @Valid @RequestBody Profile profile) {
        try {
            Profile updated = profileService.updateProfile(id, profile);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/profiles/{id}")
    public ResponseEntity<Map<String, String>> deleteProfile(@PathVariable Long id) {
        try {
            profileService.deleteProfile(id);
            return ResponseEntity.ok(Map.of("message", "Profile deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Batch ──

    @PostMapping("/profiles/batch")
    public ResponseEntity<List<Profile>> batchCreate(@Valid @RequestBody List<Profile> profiles) {
        List<Profile> created = profileService.createBatch(profiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── Search ──

    @GetMapping("/profiles/search")
    public List<Profile> searchProfiles(@RequestParam String q) {
        return profileService.searchProfiles(q);
    }

    // ── Health ──

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "profiles", profileService.countProfiles()
        ));
    }
}
