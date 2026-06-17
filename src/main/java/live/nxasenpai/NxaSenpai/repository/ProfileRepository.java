package live.nxasenpai.NxaSenpai.repository;

import live.nxasenpai.NxaSenpai.model.Profile;
import live.nxasenpai.NxaSenpai.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByRegistrationNumber(String registrationNumber);

    Optional<Profile> findByEmail(String email);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByEmail(String email);

    List<Profile> findByProfileType(ProfileType profileType);

    List<Profile> findByDepartment(String department);

    @Query("SELECT p FROM Profile p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.registrationNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Profile> search(@Param("query") String query);

    List<Profile> findByDepartmentIn(List<String> departments);
}
