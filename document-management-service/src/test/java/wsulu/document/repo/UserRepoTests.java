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
import wsulu.document.enums.Roles;
import wsulu.document.model.UserEntity;
import org.junit.jupiter.api.Test;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SuppressWarnings("unused")
public class UserRepoTests {

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
    private UserRepo userRepo;

    @Test
    public void findOneByUserNameShouldReturnLatestUserWhenExists() {
        userRepo.save(createTestUser("testUser","password1"));
        userRepo.save(createTestUser("testUser", "password2"));
        UserEntity result = userRepo.findOneByUserName("testUser");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("password2", result.getPassword());
    }

    @Test
    public void findOneByUserNameShouldReturnNullWhenNotExists() {
        userRepo.save(createTestUser("otherUser", "password"));
        UserEntity result = userRepo.findOneByUserName("nonExistingUser");
        Assertions.assertNull(result);
    }

    @Test
    public void findOneByUserNameShouldReturnUserWhenSingleExists() {
        userRepo.save(createTestUser("uniqueUser", "password"));
        UserEntity result = userRepo.findOneByUserName("uniqueUser");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(Roles.ADMIN, result.getRole());
    }

    private UserEntity createTestUser(String username, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setRole(Roles.ADMIN);
        user.setPassword(password);
        return user;
    }
}
