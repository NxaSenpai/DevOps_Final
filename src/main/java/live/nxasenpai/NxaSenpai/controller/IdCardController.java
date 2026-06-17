package live.nxasenpai.NxaSenpai.controller;

import live.nxasenpai.NxaSenpai.model.BarcodeType;
import live.nxasenpai.NxaSenpai.model.Profile;
import live.nxasenpai.NxaSenpai.model.ProfileType;
import live.nxasenpai.NxaSenpai.service.IdCardService;
import live.nxasenpai.NxaSenpai.service.ProfileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/id-cards")
public class IdCardController {

    private final IdCardService idCardService;
    private final ProfileService profileService;

    public IdCardController(IdCardService idCardService, ProfileService profileService) {
        this.idCardService = idCardService;
        this.profileService = profileService;
    }

    // ── Live Preview ──

    @GetMapping("/preview/{profileId}")
    public String preview(@PathVariable Long profileId, Model model) {
        Profile profile = profileService.getProfileById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        model.addAttribute("profile", profile);
        model.addAttribute("qrCodeBase64", idCardService.generateQrCodeBase64(profile));
        model.addAttribute("barcodeBase64",
                idCardService.generateBarcodeBase64(profile.getRegistrationNumber(), BarcodeType.CODE_128));
        return "id-card-preview";
    }

    // ── PDF Export (single) ──

    @GetMapping("/pdf/{profileId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long profileId) throws IOException {
        Profile profile = profileService.getProfileById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        byte[] pdfBytes = idCardService.generatePdfCard(profileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                profile.getRegistrationNumber() + "-id-card.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ── PDF Export (batch by selection) ──

    @PostMapping("/batch-pdf")
    public ResponseEntity<byte[]> downloadBatchPdf(@RequestParam List<Long> profileIds) throws IOException {
        byte[] zipBytes = idCardService.generateBatchPdfCards(profileIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "id-cards-batch.zip");

        return ResponseEntity.ok().headers(headers).body(zipBytes);
    }

    // ── PDF Export (batch by department) ──

    @GetMapping("/batch-pdf/by-department")
    public ResponseEntity<byte[]> downloadBatchByDepartment(@RequestParam String department) throws IOException {
        byte[] zipBytes = idCardService.generateBatchByDepartment(department);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "id-cards-" + department.replaceAll("\\s+", "-") + ".zip");

        return ResponseEntity.ok().headers(headers).body(zipBytes);
    }

    // ── Batch generation page ──

    @GetMapping("/batch")
    public String batchPage(Model model) {
        model.addAttribute("profiles", profileService.getAllProfiles());
        model.addAttribute("profileTypes", ProfileType.values());
        return "id-card-batch";
    }

    // ── QR Code image (raw) ──

    @GetMapping("/qr/{profileId}")
    public ResponseEntity<byte[]> qrCodeImage(@PathVariable Long profileId) {
        try {
            Profile profile = profileService.getProfileById(profileId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

            String data = "ID:" + profile.getRegistrationNumber()
                    + "|Name:" + profile.getFullName();
            byte[] qrBytes = idCardService.generateQrCode(data, 200, 200);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(qrBytes);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate QR code");
        }
    }

    // ── Barcode image (raw) ──

    @GetMapping("/barcode/{profileId}")
    public ResponseEntity<byte[]> barcodeImage(@PathVariable Long profileId,
                                                @RequestParam(defaultValue = "CODE_128") BarcodeType type) {
        try {
            Profile profile = profileService.getProfileById(profileId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

            byte[] barcodeBytes = idCardService.generateBarcode(
                    profile.getRegistrationNumber(), type, 300, 60);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(barcodeBytes);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate barcode");
        }
    }
}
