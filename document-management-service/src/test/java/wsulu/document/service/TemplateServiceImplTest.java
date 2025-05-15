package wsulu.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wsulu.document.model.TemplateEntity;
import wsulu.document.repo.TemplateRepo;
import wsulu.document.service.impl.TemplateServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SuppressWarnings("unused")
public class TemplateServiceImplTest {

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

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        templateService = new TemplateServiceImpl(templateRepo);
    }

    @Test
    void saveTemplateDocxShouldReturnTrueWhenSaveSuccessful() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test content".getBytes()
        );
        boolean result = templateService.saveTemplateDocx(file, "Test Template", "testUser");
        assertTrue(result);
        TemplateEntity savedTemplate = templateRepo.findFirstByTitleOrderByIdDesc("Test Template");
        assertNotNull(savedTemplate);
        assertEquals(1L, savedTemplate.getVersion());
        assertArrayEquals("test content".getBytes(), savedTemplate.getDocumentBody());
    }

    @Test
    void saveTemplateDocxShouldIncrementVersionWhenTemplateExists() {
        TemplateEntity existingTemplate = new TemplateEntity();
        existingTemplate.setTitle("Existing Template");
        existingTemplate.setDocumentBody("v1 content".getBytes());
        existingTemplate.setVersion(1L);
        existingTemplate.setUserName("oldUser");
        templateRepo.save(existingTemplate);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "template.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "v2 content".getBytes()
        );
        boolean result = templateService.saveTemplateDocx(file, "Existing Template", "newUser");
        assertTrue(result);
        TemplateEntity savedTemplate = templateRepo.findFirstByTitleOrderByIdDesc("Existing Template");
        assertNotNull(savedTemplate);
        assertEquals(2L, savedTemplate.getVersion());
        assertArrayEquals("v2 content".getBytes(), savedTemplate.getDocumentBody());
        assertEquals("newUser", savedTemplate.getUserName());
    }

    @Test
    void saveTemplateDocxShouldReturnFalseWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[0]
        );
        boolean result = templateService.saveTemplateDocx(emptyFile, "Empty Template", "testUser");
        assertFalse(result);
    }

    @Test
    void getTemplateByIdShouldReturnTemplateWhenExists() {
        TemplateEntity template = new TemplateEntity();
        template.setTitle("Test Template");
        template.setDocumentBody("test content".getBytes());
        template.setVersion(1L);
        template.setUserName("testUser");
        TemplateEntity savedTemplate = templateRepo.save(template);
        byte[] result = templateService.getTemplateById(savedTemplate.getId());
        assertArrayEquals("test content".getBytes(), result);
    }

    @Test
    void getTemplateByIdShouldReturnEmptyWhenNotExists() {
        byte[] result = templateService.getTemplateById(999L);
        assertEquals(0, result.length);
    }

    @Test
    void getLastTemplateBodyByTitleShouldReturnLatestVersion() {
        TemplateEntity v1 = new TemplateEntity();
        v1.setTitle("Versioned Template");
        v1.setDocumentBody("v1 content".getBytes());
        v1.setVersion(1L);
        templateRepo.save(v1);
        TemplateEntity v2 = new TemplateEntity();
        v2.setTitle("Versioned Template");
        v2.setDocumentBody("v2 content".getBytes());
        v2.setVersion(2L);
        templateRepo.save(v2);
        byte[] result = templateService.getLastTemplateBodyByTitle("Versioned Template");
        assertArrayEquals("v2 content".getBytes(), result);
    }

    @Test
    void getLastTemplateBodyByTitleShouldReturnEmptyWhenNotExists() {
        byte[] result = templateService.getLastTemplateBodyByTitle("Non-existent Template");
        assertEquals(0, result.length);
    }
}
