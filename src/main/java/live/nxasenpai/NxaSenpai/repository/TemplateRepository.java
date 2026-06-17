package live.nxasenpai.NxaSenpai.repository;

import live.nxasenpai.NxaSenpai.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByName(String name);

    boolean existsByName(String name);

    List<Template> findByTemplateType(String templateType);

    @Query("SELECT t FROM Template t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Template> search(@Param("query") String query);
}
