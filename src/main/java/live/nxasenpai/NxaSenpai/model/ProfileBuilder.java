package live.nxasenpai.NxaSenpai.model;

import java.time.LocalDate;

/**
 * Builder utility to construct Profile objects with sensible defaults.
 * Usage:
 *   Profile profile = new ProfileBuilder()
 *       .withProfileType(ProfileType.STUDENT)
 *       .withFirstName("John")
 *       .withLastName("Doe")
 *       .withEmail("john@example.com")
 *       .withDepartment("Computer Science")
 *       .withRegistrationNumber("2026-CS-001")
 *       .build();
 */
public class ProfileBuilder {

    private ProfileType profileType = ProfileType.USER;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String department;
    private String photoPath;
    private LocalDate dateOfBirth;
    private String address;

    public ProfileBuilder withProfileType(ProfileType profileType) {
        this.profileType = profileType;
        return this;
    }

    public ProfileBuilder withRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        return this;
    }

    public ProfileBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ProfileBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ProfileBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public ProfileBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public ProfileBuilder withDepartment(String department) {
        this.department = department;
        return this;
    }

    public ProfileBuilder withPhotoPath(String photoPath) {
        this.photoPath = photoPath;
        return this;
    }

    public ProfileBuilder withDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public ProfileBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    public Profile build() {
        return new Profile(profileType, registrationNumber, firstName, lastName,
                email, phone, department, photoPath, dateOfBirth, address);
    }

    /**
     * Creates a default student profile (empty fields, STUDENT type).
     */
    public static ProfileBuilder defaultStudent() {
        return new ProfileBuilder().withProfileType(ProfileType.STUDENT);
    }

    /**
     * Creates a default employee profile (empty fields, EMPLOYEE type).
     */
    public static ProfileBuilder defaultEmployee() {
        return new ProfileBuilder().withProfileType(ProfileType.EMPLOYEE);
    }
}
