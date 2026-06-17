# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build and run all tests
./gradlew build

# Run only tests (skip jar)
./gradlew test

# Run a single test class
./gradlew test --tests "live.nxasenpai.NxaSenpai.service.ProfileServiceTest"

# Run the application (H2 in-memory DB by default)
./gradlew bootRun

# Clean and rebuild
./gradlew clean build
```

## Architecture

- **Stack:** Spring Boot 4.1.0, Java 25, Gradle 9.5.1 (Kotlin DSL).
- **Group/Artifact:** `live.nxasenpai` / `NxaSenpai`.
- **Purpose:** ID Card management system for students and employees.

### Layers

```
controller/          → Thymeleaf views + REST API (/api)
    HomeController   → redirects "/" to "/profiles"
    ProfileController→ /profiles (CRUD, photo upload, batch form)
    TemplateController→ /templates (CRUD for card templates)
    IdCardController → /id-cards (preview, PDF export, batch ZIP download, QR/barcode images)
    ApiController    → /api/profiles (REST CRUD, batch, search, health)

service/
    ProfileService   → CRUD, photo upload (local filesystem: uploads/photos/), search, batch create
    TemplateService  → CRUD for ID card templates
    IdCardService    → PDF generation (iText 8), QR codes (ZXing), barcodes (ZXing Code-128/EAN-13),
                       Thymeleaf HTML preview rendering, batch PDF-to-ZIP export

model/
    Profile          → JPA entity (id, profileType, registrationNumber, firstName, lastName,
                       email, phone, department, photoPath, dateOfBirth, address, timestamps)
    ProfileType      → enum: STUDENT, EMPLOYEE, USER
    ProfileBuilder   → builder pattern for constructing Profile with defaults
    Template         → JPA entity (id, name, description, templateType [HTML|PDF], htmlContent, timestamps)
    BarcodeType      → enum: CODE_128, EAN_13

repository/
    ProfileRepository  → JPA repo with search, findByProfileType, findByDepartment, existsByX
    TemplateRepository → JPA repo with search, findByName, findByTemplateType

util/
    RegistrationNumberGenerator → YEAR-DEPT-#### format (e.g., 2026-CS-0042); UUID fallback
    PhotoValidator              → JPEG/PNG validation, 5MB max
config/
    WebConfig         → serves /photos/** from local uploads/photos/ directory
```

### Database

- **Default (dev):** H2 in-memory (`jdbc:h2:mem:nxasenpaidb`), auto-schema via `ddl-auto=update`.
- **Production:** MySQL — uncomment the MySQL datasource block in `application.properties`.
- H2 Console available at `/h2-console` when using H2.

### Thymeleaf Templates

- `fragments/layout.html` — common layout with Bootstrap 5 navbar, alert messages
- `id-card-preview.html` — live preview card with photo, QR code, barcode
- `id-card-batch.html` — batch download by selection or department
- `profile/list.html`, `profile/view.html`, `profile/form.html`, `profile/batch.html`
- `template/list.html`, `template/view.html`, `template/form.html`

### Key URLs

| Path | Description |
|------|-------------|
| `/profiles` | Profile list + search/filter |
| `/profiles/new` | Create profile |
| `/profiles/{id}` | View profile + photo upload |
| `/templates` | Template management |
| `/id-cards/preview/{profileId}` | Live ID card preview |
| `/id-cards/pdf/{profileId}` | Download single PDF |
| `/id-cards/batch` | Batch generation page |
| `/api/profiles` | REST API (JSON) |
| `/api/health` | Health check |
