package wsulu.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wsulu.document.dto.RequestDtoWrapper;
import wsulu.document.model.DocumentEntity;
import wsulu.document.model.DocumentsContractEntity;
import wsulu.document.repo.DocumentsContractRepo;
import wsulu.document.service.impl.DocumentsContractServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class DocumentsContractServiceImplTest {

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
    private DocumentsContractRepo documentsContractRepo;

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private DocumentsContractServiceImpl documentsContractService;

    @BeforeEach
    void setUp() {
        documentsContractService = new DocumentsContractServiceImpl(documentsContractRepo, templateService);
    }

    @Test
    void getDocumentsByLoanShouldReturnEmptyWhenContractNotFound() {
        byte[] result = documentsContractService.getDocumentsByLoan(1L, 1L, "testTemplate");
        assertArrayEquals(new byte[0], result);
    }

    @Test
    void getDocumentsByLoan_ShouldReturnDocument_WhenExists() {
        DocumentsContractEntity contract = new DocumentsContractEntity();
        contract.setLoanId(1L);
        contract.setClientId(1L);
        contract.setContractNumber("123");
        contract.setMobile("123");
        Set<DocumentEntity> documents = new HashSet<>();
        DocumentEntity doc = new DocumentEntity();
        doc.setDocumentTitle("testTemplate");
        doc.setVersion(1L);
        doc.setContractPdf("test content".getBytes());
        doc.setDocumentsContract(contract);
        documents.add(doc);
        contract.setAllDocuments(documents);
        documentsContractRepo.save(contract);
        byte[] result = documentsContractService.getDocumentsByLoan(1L, 1L, "testTemplate");
        assertArrayEquals("test content".getBytes(), result);
    }

    @Test
    void previewDocumentsContractShouldReturnNullWhenInvalidData() {
        RequestDtoWrapper emptyRequest = new RequestDtoWrapper();
        assertNull(documentsContractService.previewDocumentsContract(emptyRequest));
        RequestDtoWrapper noTemplateRequest = new RequestDtoWrapper();
        noTemplateRequest.setData(List.of(Map.of("otherField", "value")));
        assertNull(documentsContractService.previewDocumentsContract(noTemplateRequest));
    }

    @Test
    void generateAndSaveShouldReturnFalseWhenInvalidData() {
        assertFalse(documentsContractService.generateAndSave(Collections.emptyList()));
        assertFalse(documentsContractService.generateAndSave(List.of(
                Map.of("otherField", "value")
        )));
    }
}
