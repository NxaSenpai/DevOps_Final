package live.nxasenpai.NxaSenpai.controller;

import jakarta.validation.Valid;
import live.nxasenpai.NxaSenpai.model.Profile;
import live.nxasenpai.NxaSenpai.model.ProfileType;
import live.nxasenpai.NxaSenpai.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // ── List ──

    @GetMapping
    public String listProfiles(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String department) {
        List<Profile> profiles;
        if (search != null && !search.isBlank()) {
            profiles = profileService.searchProfiles(search);
        } else if (type != null && !type.isBlank()) {
            profiles = profileService.getProfilesByType(ProfileType.valueOf(type.toUpperCase()));
        } else if (department != null && !department.isBlank()) {
            profiles = profileService.getProfilesByDepartment(department);
        } else {
            profiles = profileService.getAllProfiles();
        }
        model.addAttribute("profiles", profiles);
        model.addAttribute("profileTypes", ProfileType.values());
        model.addAttribute("search", search);
        return "profile/list";
    }

    // ── View ──

    @GetMapping("/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        Profile profile = profileService.getProfileById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        model.addAttribute("profile", profile);
        return "profile/view";
    }

    // ── Create form ──

    @GetMapping("/new")
    public String newProfileForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("profileTypes", ProfileType.values());
        return "profile/form";
    }

    // ── Create ──

    @PostMapping
    public String createProfile(@Valid @ModelAttribute Profile profile,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("profileTypes", ProfileType.values());
            return "profile/form";
        }
        profileService.createProfile(profile);
        return "redirect:/profiles";
    }

    // ── Edit form ──

    @GetMapping("/{id}/edit")
    public String editProfileForm(@PathVariable Long id, Model model) {
        Profile profile = profileService.getProfileById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        model.addAttribute("profile", profile);
        model.addAttribute("profileTypes", ProfileType.values());
        return "profile/form";
    }

    // ── Update ──

    @PostMapping("/{id}")
    public String updateProfile(@PathVariable Long id,
                                @Valid @ModelAttribute Profile profile,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("profileTypes", ProfileType.values());
            return "profile/form";
        }
        profileService.updateProfile(id, profile);
        return "redirect:/profiles/" + id;
    }

    // ── Delete ──

    @PostMapping("/{id}/delete")
    public String deleteProfile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            profileService.deleteProfile(id);
            redirectAttributes.addFlashAttribute("message", "Profile deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete profile: " + e.getMessage());
        }
        return "redirect:/profiles";
    }

    // ── Photo Upload ──

    @PostMapping("/{id}/photo")
    public String uploadPhoto(@PathVariable Long id,
                              @RequestParam("photo") MultipartFile photo,
                              RedirectAttributes redirectAttributes) {
        try {
            profileService.uploadPhoto(id, photo);
            redirectAttributes.addFlashAttribute("message", "Photo uploaded successfully.");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("error", "Photo upload failed: " + e.getMessage());
        }
        return "redirect:/profiles/" + id;
    }

    // ── Batch Create Form ──

    @GetMapping("/batch")
    public String batchForm(Model model) {
        model.addAttribute("profileTypes", ProfileType.values());
        return "profile/batch";
    }
}
