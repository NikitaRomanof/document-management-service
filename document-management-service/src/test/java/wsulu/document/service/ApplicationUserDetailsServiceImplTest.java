package wsulu.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import wsulu.document.enums.Roles;
import wsulu.document.model.UserEntity;
import wsulu.document.repo.UserRepo;
import wsulu.document.service.impl.ApplicationUserDetailsServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SuppressWarnings("unused")
public class ApplicationUserDetailsServiceImplTest {

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

    private ApplicationUserDetailsServiceImpl userDetailsService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userDetailsService = new ApplicationUserDetailsServiceImpl(userRepo);
    }

    @Test
    void loadUserByUsernameShouldReturnUserWhenExists() {
        UserEntity user = UserEntity.builder()
                .username("testUser")
                .password(passwordEncoder.encode("password"))
                .role(Roles.USER)
                .build();
        userRepo.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername("testUser");
        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertTrue(passwordEncoder.matches("password", userDetails.getPassword()));
    }

    @Test
    void loadUserByUsernameShouldNullWhenUserNotFound() {
        assertNull(userDetailsService.loadUserByUsername("nonExistingUser"));
    }

    @Test
    void createShouldSaveUserWithCorrectRole() {
        String result = userDetailsService.create("newUser", "newPassword", "ADMIN");
        assertEquals("Create Successfully !", result);
        UserEntity savedUser = userRepo.findOneByUserName("newUser");
        assertNotNull(savedUser);
        assertEquals("newUser", savedUser.getUsername());
        assertTrue(passwordEncoder.matches("newPassword", savedUser.getPassword()));
        assertEquals(Roles.ADMIN, savedUser.getRole());
    }

    @Test
    void createShouldUseDefaultRoleWhenInvalidRoleProvided() {
        String result = userDetailsService.create("newUser", "newPassword", "INVALID_ROLE");
        assertEquals("Create Successfully !", result);
        UserEntity savedUser = userRepo.findOneByUserName("newUser");
        assertNotNull(savedUser);
        assertEquals(Roles.USER, savedUser.getRole());
    }

    @Test
    void createShouldEncodePassword() {
        userDetailsService.create("newUser", "plainPassword", "USER");
        UserEntity savedUser = userRepo.findOneByUserName("newUser");
        assertNotNull(savedUser);
        assertNotEquals("plainPassword", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("plainPassword", savedUser.getPassword()));
    }
}
