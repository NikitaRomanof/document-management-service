package wsulu.document.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wsulu.document.model.TemplateEntity;

@Repository
public interface TemplateRepo extends JpaRepository<TemplateEntity, Long> {

    @Query(
            value =
                    "SELECT * FROM document_template AS template "
                            + "WHERE template.title=:title order by template.id desc limit 1",
            nativeQuery = true)
    TemplateEntity findFirstByTitleOrderByIdDesc(@Param("title") String title);
}
