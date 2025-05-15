package wsulu.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wsulu.document.model.DocumentEntity;
import wsulu.document.model.DocumentsContractEntity;
import wsulu.document.repo.DocumentsContractRepo;
import wsulu.document.service.impl.DocumentZipServiceImpl;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class DocumentZipServiceImplTest {

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

    @InjectMocks
    private DocumentZipServiceImpl documentZipService;

    private DocumentsContractEntity contract;

    @BeforeEach
    void setUp() {
        documentZipService = new DocumentZipServiceImpl(documentsContractRepo);
        contract = new DocumentsContractEntity();
        contract.setLoanId(1L);
        contract.setClientId(1L);
        contract.setContractNumber("123");
        contract.setMobile("123");
    }

    @Test
    void getAllDocumentByLoanIdShouldReturnEmptyWhenContractNotFound() {
        byte[] result = documentZipService.getAllDocumentByLoanId(1L, 1L, false);
        assertEquals(0, result.length);
    }

    @Test
    void getAllDocumentByLoanIdShouldReturnZipWithSingleDocumentWhenOneVersionExists() {
        Set<DocumentEntity> documents = new HashSet<>();
        DocumentEntity doc = new DocumentEntity();
        doc.setDocumentTitle("testTemplate");
        doc.setVersion(1L);
        doc.setContractPdf("test content".getBytes());
        doc.setDocumentsContract(contract);
        documents.add(doc);
        contract.setAllDocuments(documents);
        documentsContractRepo.save(contract);
        byte[] result = documentZipService.getAllDocumentByLoanId(1L, 1L, false);
        assertTrue(result.length > 0);
    }

    @Test
    void getAllDocumentByLoanIdShouldReturnZipWithAllVersionsWhenAllVersionTrue() {
        Set<DocumentEntity> documents = new HashSet<>();
        DocumentEntity doc1 = new DocumentEntity();
        doc1.setDocumentTitle("testDoc");
        doc1.setVersion(1L);
        doc1.setContractPdf("version 1".getBytes());
        doc1.setDocumentsContract(contract);
        DocumentEntity doc2 = new DocumentEntity();
        doc2.setDocumentTitle("testDoc");
        doc2.setVersion(2L);
        doc2.setContractPdf("version 2".getBytes());
        doc2.setDocumentsContract(contract);
        documents.add(doc1);
        documents.add(doc2);
        contract.setAllDocuments(documents);
        documentsContractRepo.save(contract);
        byte[] result = documentZipService.getAllDocumentByLoanId(1L, 1L, true);
        assertTrue(result.length > 0);
    }

    @Test
    void getAllDocumentByLoanIdShouldReturnZipWithLatestVersionWhenAllVersionFalse() {
        Set<DocumentEntity> documents = new HashSet<>();
        DocumentEntity doc1 = new DocumentEntity();
        doc1.setDocumentTitle("testDoc");
        doc1.setVersion(1L);
        doc1.setContractPdf("version 1".getBytes());
        doc1.setDocumentsContract(contract);
        DocumentEntity doc2 = new DocumentEntity();
        doc2.setDocumentTitle("testDoc");
        doc2.setVersion(2L);
        doc2.setContractPdf("version 2".getBytes());
        doc2.setDocumentsContract(contract);
        documents.add(doc1);
        documents.add(doc2);
        contract.setAllDocuments(documents);
        documentsContractRepo.save(contract);
        byte[] result = documentZipService.getAllDocumentByLoanId(1L, 1L, false);
        assertTrue(result.length > 0);
    }

    @Test
    void getLastVersionDocumentByLoanIdShouldReturnOnlyLatestVersions() {
        Set<DocumentEntity> documents = new HashSet<>();
        DocumentEntity doc1 = new DocumentEntity();
        doc1.setDocumentTitle("doc1");
        doc1.setVersion(1L);
        doc1.setContractPdf("content".getBytes());
        doc1.setDocumentsContract(contract);
        DocumentEntity doc2 = new DocumentEntity();
        doc2.setDocumentTitle("doc1");
        doc2.setVersion(2L);
        doc2.setContractPdf("content".getBytes());
        doc2.setDocumentsContract(contract);
        DocumentEntity doc3 = new DocumentEntity();
        doc3.setDocumentTitle("doc2");
        doc3.setVersion(1L);
        doc3.setContractPdf("content".getBytes());
        doc3.setDocumentsContract(contract);
        documents.add(doc1);
        documents.add(doc2);
        documents.add(doc3);
        contract.setAllDocuments(documents);
        documentsContractRepo.save(contract);
        byte[] resultAll = documentZipService.getAllDocumentByLoanId(1L, 1L, true);
        byte[] resultOne = documentZipService.getAllDocumentByLoanId(1L, 1L, false);
        assertTrue(resultAll.length > resultOne.length);
    }
}
