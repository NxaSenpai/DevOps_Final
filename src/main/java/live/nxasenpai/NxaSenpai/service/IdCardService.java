package live.nxasenpai.NxaSenpai.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import live.nxasenpai.NxaSenpai.model.BarcodeType;
import live.nxasenpai.NxaSenpai.model.Profile;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class IdCardService {

    private final TemplateEngine templateEngine;
    private final ProfileService profileService;

    private static final int CARD_WIDTH = 540;
    private static final int CARD_HEIGHT = 340;
    private static final int QR_SIZE = 200;
    private static final int BARCODE_WIDTH = 300;
    private static final int BARCODE_HEIGHT = 60;

    public IdCardService(TemplateEngine templateEngine, ProfileService profileService) {
        this.templateEngine = templateEngine;
        this.profileService = profileService;
    }

    // ── Live Preview (Thymeleaf HTML) ──

    /**
     * Renders an HTML preview of the ID card for a given profile.
     */
    public String renderPreview(Long profileId) {
        Profile profile = profileService.getProfileById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + profileId));

        Context context = new Context();
        context.setVariable("profile", profile);
        context.setVariable("qrCodeBase64", generateQrCodeBase64(profile));

        return templateEngine.process("id-card-preview", context);
    }

    // ── PDF Export ──

    /**
     * Generates a single ID card as a PDF byte array.
     */
    public byte[] generatePdfCard(Long profileId) throws IOException {
        Profile profile = profileService.getProfileById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + profileId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, new com.itextpdf.kernel.geom.PageSize(CARD_WIDTH, CARD_HEIGHT));
        document.setMargins(15, 15, 15, 15);

        addCardContent(document, profile);
        document.close();

        return baos.toByteArray();
    }

    /**
     * Batch: generates PDF cards for multiple profiles, returned as a ZIP.
     */
    public byte[] generateBatchPdfCards(List<Long> profileIds) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (Long profileId : profileIds) {
            Profile profile = profileService.getProfileById(profileId)
                    .orElse(null);
            if (profile == null) continue;

            byte[] cardPdf = generatePdfCard(profileId);
            ZipEntry entry = new ZipEntry(profile.getRegistrationNumber() + ".pdf");
            zos.putNextEntry(entry);
            zos.write(cardPdf);
            zos.closeEntry();
        }
        zos.close();
        return baos.toByteArray();
    }

    /**
     * Batch: generates PDF cards for all profiles matching a department.
     */
    public byte[] generateBatchByDepartment(String department) throws IOException {
        List<Profile> profiles = profileService.getProfilesByDepartment(department);
        List<Long> ids = profiles.stream().map(Profile::getId).toList();
        return generateBatchPdfCards(ids);
    }

    // ── QR Code Generation ──

    /**
     * Generates a QR code PNG image as a byte array.
     */
    public byte[] generateQrCode(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.MARGIN, 1,
                EncodeHintType.CHARACTER_SET, "UTF-8"
        );
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
        return pngOut.toByteArray();
    }

    /**
     * Generates a QR code for a profile and returns it as a Base64 data URI.
     */
    public String generateQrCodeBase64(Profile profile) {
        try {
            String data = buildVerificationData(profile);
            byte[] qrBytes = generateQrCode(data, QR_SIZE, QR_SIZE);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            return "";
        }
    }

    // ── Barcode Generation ──

    /**
     * Generates a barcode image (Code-128 or EAN-13) as a byte array.
     */
    public byte[] generateBarcode(String data, BarcodeType barcodeType, int width, int height)
            throws WriterException, IOException {
        BitMatrix bitMatrix;

        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.MARGIN, 1,
                EncodeHintType.CHARACTER_SET, "UTF-8"
        );

        if (barcodeType == BarcodeType.EAN_13) {
            // EAN-13 must be exactly 12 or 13 digits
            String eanData = data.replaceAll("[^0-9]", "");
            if (eanData.length() > 13) eanData = eanData.substring(0, 13);
            if (eanData.length() < 12) eanData = String.format("%012d", Long.parseLong(eanData.isEmpty() ? "0" : eanData));
            if (eanData.length() == 12) eanData = eanData; // leave 12-digit input as-is; EAN13Writer handles check digit
            EAN13Writer writer = new EAN13Writer();
            bitMatrix = writer.encode(eanData, BarcodeFormat.EAN_13, width, height, hints);
        } else {
            Code128Writer writer = new Code128Writer();
            bitMatrix = writer.encode(data, BarcodeFormat.CODE_128, width, height, hints);
        }

        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
        return pngOut.toByteArray();
    }

    public String generateBarcodeBase64(String data, BarcodeType barcodeType) {
        try {
            byte[] barcodeBytes = generateBarcode(data, barcodeType, BARCODE_WIDTH, BARCODE_HEIGHT);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(barcodeBytes);
        } catch (Exception e) {
            return "";
        }
    }

    // ── Private helpers ──

    private void addCardContent(Document document, Profile profile) throws IOException {
        PdfFont regular;
        try {
            regular = PdfFontFactory.createFont();
        } catch (Exception e) {
            regular = PdfFontFactory.createFont();
        }

        DeviceRgb primaryColor = new DeviceRgb(30, 64, 175); // deep blue
        DeviceRgb accentColor = new DeviceRgb(59, 130, 246); // lighter blue
        DeviceRgb textColor = new DeviceRgb(30, 41, 59);

        // Header bar
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}));
        header.setWidth(UnitValue.createPercentValue(100));
        Cell headerCell = new Cell()
                .setBackgroundColor(primaryColor)
                .setPadding(8)
                .add(new Paragraph("ID CARD")
                        .setFontColor(ColorConstants.WHITE)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER));
        header.addCell(headerCell);
        document.add(header);

        // Body: two-column layout: photo | details
        Table body = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        body.setWidth(UnitValue.createPercentValue(100));
        body.setMarginTop(10);

        // Photo cell
        Cell photoCell = new Cell().setBorder(new SolidBorder(accentColor, 1)).setPadding(4);
        try {
            Path photoPath = Paths.get(profile.getPhotoPath() != null
                    ? "uploads/photos/" + profile.getPhotoPath().replace("/photos/", "")
                    : "");
            if (Files.exists(photoPath)) {
                Image photo = new Image(ImageDataFactory.create(photoPath.toString()));
                photo.setAutoScale(true);
                photoCell.add(photo);
            } else {
                photoCell.add(new Paragraph("[No Photo]")
                        .setFontSize(8).setTextAlignment(TextAlignment.CENTER));
            }
        } catch (Exception e) {
            photoCell.add(new Paragraph("[No Photo]")
                    .setFontSize(8).setTextAlignment(TextAlignment.CENTER));
        }
        body.addCell(photoCell);

        // Details cell
        Cell detailsCell = new Cell().setPadding(6).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        detailsCell.add(new Paragraph(profile.getFullName())
                .setFontSize(16).setFontColor(primaryColor));
        detailsCell.add(new Paragraph(profile.getProfileType().toString())
                .setFontSize(10).setFontColor(accentColor));
        detailsCell.add(new Paragraph("Reg#: " + profile.getRegistrationNumber())
                .setFontSize(9).setFontColor(textColor));
        if (profile.getDepartment() != null) {
            detailsCell.add(new Paragraph("Dept: " + profile.getDepartment())
                    .setFontSize(9).setFontColor(textColor));
        }
        if (profile.getEmail() != null) {
            detailsCell.add(new Paragraph("Email: " + profile.getEmail())
                    .setFontSize(8).setFontColor(textColor));
        }
        if (profile.getPhone() != null) {
            detailsCell.add(new Paragraph("Phone: " + profile.getPhone())
                    .setFontSize(8).setFontColor(textColor));
        }
        body.addCell(detailsCell);
        document.add(body);

        // Footer with QR code and barcode
        Table footer = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        footer.setWidth(UnitValue.createPercentValue(100));
        footer.setMarginTop(8);

        // QR code
        Cell qrCell = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(4);
        try {
            String qrData = buildVerificationData(profile);
            byte[] qrBytes = generateQrCode(qrData, 80, 80);
            Image qrImage = new Image(ImageDataFactory.create(qrBytes));
            qrCell.add(qrImage);
        } catch (Exception e) {
            qrCell.add(new Paragraph("[QR Error]").setFontSize(6));
        }
        footer.addCell(qrCell);

        // Barcode
        Cell barcodeCell = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(4)
                .setTextAlignment(TextAlignment.RIGHT);
        try {
            byte[] barcodeBytes = generateBarcode(profile.getRegistrationNumber(),
                    BarcodeType.CODE_128, 200, 40);
            Image barcodeImage = new Image(ImageDataFactory.create(barcodeBytes));
            barcodeCell.add(barcodeImage);
            barcodeCell.add(new Paragraph(profile.getRegistrationNumber())
                    .setFontSize(6).setTextAlignment(TextAlignment.CENTER));
        } catch (Exception e) {
            barcodeCell.add(new Paragraph("[Barcode Error]").setFontSize(6));
        }
        footer.addCell(barcodeCell);
        document.add(footer);
    }

    private String buildVerificationData(Profile profile) {
        return "ID:" + profile.getRegistrationNumber()
                + "|Name:" + profile.getFullName()
                + "|Type:" + profile.getProfileType()
                + "|Dept:" + (profile.getDepartment() != null ? profile.getDepartment() : "N/A");
    }
}
