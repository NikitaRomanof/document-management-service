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
import wsulu.document.model.DocumentsContractEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SuppressWarnings("unused")
public class DocumentsContractRepoTests {

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

    @Test
    public void findFirstByLoanIdShouldReturnDocumentWhenExists() {
        DocumentsContractEntity entity1 = createTestContractEntity(1L, 100L, "1234567890");
        DocumentsContractEntity entity2 = createTestContractEntity(1L, 100L, "1234567891");
        documentsContractRepo.save(entity1);
        documentsContractRepo.save(entity2);
        DocumentsContractEntity result = documentsContractRepo.findFirstByLoanId(1L, 100L);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("1234567891", result.getContractNumber());
    }

    @Test
    public void countByMobileShouldReturnCorrectCount() {
        documentsContractRepo.save(createTestContractEntity(1L, 100L, "1234567890", "79991234567"));
        documentsContractRepo.save(createTestContractEntity(2L, 101L, "1234567891", "79991234567"));
        documentsContractRepo.save(createTestContractEntity(3L, 102L, "1234567892", "79997654321"));
        long count = documentsContractRepo.countByMobile("79991234567");
        Assertions.assertEquals(2, count);
    }

    @Test
    public void findFirstByClientIdOrderByIdDescShouldReturnLatestDocument() {
        documentsContractRepo.save(createTestContractEntity(1L, 100L, "CONTRACT-1"));
        documentsContractRepo.save(createTestContractEntity(2L, 100L, "CONTRACT-2"));
        DocumentsContractEntity result = documentsContractRepo.findFirstByClientIdOrderByIdDesc(100L);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("CONTRACT-2", result.getContractNumber());
    }

    @Test
    public void findAllByMobileShouldReturnAllDocumentsWithMobile() {
        documentsContractRepo.save(createTestContractEntity(1L, 100L, "CNT-1", "79991234567"));
        documentsContractRepo.save(createTestContractEntity(2L, 101L, "CNT-2", "79991234567"));
        documentsContractRepo.save(createTestContractEntity(3L, 102L, "CNT-3", "79997654321"));
        List<DocumentsContractEntity> results = documentsContractRepo.findAllByMobile("79991234567");
        Assertions.assertEquals(2, results.size());
        Assertions.assertTrue(results.stream().allMatch(e -> "79991234567".equals(e.getMobile())));
    }

    @Test
    public void findFirstByContractNumberOrderByIdDescShouldReturnLatestDocument() {
        documentsContractRepo.save(createTestContractEntity(1L, 100L, "DUPLICATE-CN"));
        documentsContractRepo.save(createTestContractEntity(2L, 101L, "DUPLICATE-CN"));
        DocumentsContractEntity result = documentsContractRepo.findFirstByContractNumberOrderByIdDesc("DUPLICATE-CN");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2L, result.getLoanId());
    }

    private DocumentsContractEntity createTestContractEntity(Long loanId, Long clientId, String contractNumber) {
        return createTestContractEntity(loanId, clientId, contractNumber, "1234567");
    }

    private DocumentsContractEntity createTestContractEntity(Long loanId, Long clientId, String contractNumber, String mobile) {
        DocumentsContractEntity entity = new DocumentsContractEntity();
        entity.setLoanId(loanId);
        entity.setClientId(clientId);
        entity.setContractNumber(contractNumber);
        entity.setMobile(mobile);
        return entity;
    }
}
