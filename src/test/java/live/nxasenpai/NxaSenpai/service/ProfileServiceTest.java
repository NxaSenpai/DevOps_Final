package live.nxasenpai.NxaSenpai.service;

import live.nxasenpai.NxaSenpai.model.Profile;
import live.nxasenpai.NxaSenpai.model.ProfileBuilder;
import live.nxasenpai.NxaSenpai.model.ProfileType;
import live.nxasenpai.NxaSenpai.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private live.nxasenpai.NxaSenpai.util.RegistrationNumberGenerator regNumberGenerator;
    @Mock
    private live.nxasenpai.NxaSenpai.util.PhotoValidator photoValidator;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, regNumberGenerator, photoValidator);
    }

    @Test
    void profileBuilder_ShouldCreateProfileWithCorrectValues() {
        Profile profile = new ProfileBuilder()
                .withProfileType(ProfileType.STUDENT)
                .withFirstName("Alice")
                .withLastName("Smith")
                .withEmail("alice@example.com")
                .withDepartment("Computer Science")
                .withRegistrationNumber("2026-CS-0001")
                .build();

        assertEquals(ProfileType.STUDENT, profile.getProfileType());
        assertEquals("Alice", profile.getFirstName());
        assertEquals("Alice Smith", profile.getFullName());
        assertEquals("2026-CS-0001", profile.getRegistrationNumber());
    }

    @Test
    void profileBuilder_DefaultStudent_ShouldSetStudentType() {
        Profile profile = ProfileBuilder.defaultStudent()
                .withFirstName("Bob")
                .withLastName("Jones")
                .withEmail("bob@example.com")
                .withDepartment("Math")
                .withRegistrationNumber("2026-MA-0001")
                .build();

        assertEquals(ProfileType.STUDENT, profile.getProfileType());
    }

    @Test
    void profileBuilder_DefaultEmployee_ShouldSetEmployeeType() {
        Profile profile = ProfileBuilder.defaultEmployee()
                .withFirstName("Carol")
                .withLastName("Davis")
                .withEmail("carol@example.com")
                .withDepartment("HR")
                .withRegistrationNumber("2026-HR-0001")
                .build();

        assertEquals(ProfileType.EMPLOYEE, profile.getProfileType());
    }

    @Test
    void profile_FullName_ShouldConcatenateFirstAndLast() {
        Profile profile = new Profile();
        profile.setFirstName("John");
        profile.setLastName("Doe");

        assertEquals("John Doe", profile.getFullName());
    }

    @Test
    void profile_DefaultConstructor_ShouldHaveUserProfileType() {
        Profile profile = new Profile();
        assertEquals(ProfileType.USER, profile.getProfileType());
    }
}
