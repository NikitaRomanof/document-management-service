package wsulu.document.repo;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wsulu.document.model.TemplateEntity;
import org.junit.jupiter.api.Test;

import java.util.Date;

///** Для тестов репозитория необходимо запустить докер, все тесты работают в тестКонтейнере */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SuppressWarnings("unused")
public class TemplateRepoTests {

  @Container
  @SuppressWarnings("resource")
  static PostgreSQLContainer<?> database =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("springboot")
          .withPassword("springboot")
          .withUsername("springboot");

  @DynamicPropertySource
  static void setDatasourceProperties(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.datasource.url", database::getJdbcUrl);
    propertyRegistry.add("spring.datasource.password", database::getPassword);
    propertyRegistry.add("spring.datasource.username", database::getUsername);
  }

  @Autowired
  private TemplateRepo templateRepo;

  @Test
  public void findFirstByTitleOrderByIdDesc() {
    templateRepo.save(createTestTemplateEntity("test", 1L, new byte[0]));
    templateRepo.save(createTestTemplateEntity("test", 2L, new byte[0]));
    var test = templateRepo.findFirstByTitleOrderByIdDesc("test").getVersion();
    Assertions.assertEquals(2, test);
  }

  @Test
  public void negativeFindFirstByTitleOrderByIdDesc() {
    templateRepo.save(createTestTemplateEntity("test1", 1L, new byte[0]));
    templateRepo.save(createTestTemplateEntity("test1", 2L, new byte[0]));
    var test = templateRepo.findFirstByTitleOrderByIdDesc("test2");
    Assertions.assertNull(test);
  }

  private TemplateEntity createTestTemplateEntity(String title,
                                                  Long version,
                                                  byte[] body) {
    TemplateEntity templateEntity = new TemplateEntity();
    templateEntity.setTitle(title);
    templateEntity.setVersion(version);
    templateEntity.setDocumentBody(body);
    templateEntity.setCreateDate(new Date());
    templateEntity.setUserName("TestUser");
    return templateEntity;
  }
}
