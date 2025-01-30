package pl.chat.groupchat.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.chat.groupchat.exceptions.InvalidDataInputException;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.exceptions.UserAlreadyExistsException;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.UserRepository;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class UserServiceTests {

    @Container
    static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0.26")
            .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("rootTest");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void clearTestDataBase() {
        userRepository.deleteAll();
    }

    @Test
    void testFindById_userFound() {
        User testUser = initializeTestUser();

        User foundUser = userService.findUserById(testUser.getId());

        assertNotNull(foundUser, "Should be not null");
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertEquals(testUser.getPassword(), foundUser.getPassword());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        assertTrue(foundUser.isActive());
    }

    @Test
    void testFindById_userNotFound_throwsException() {
        int wrongId = -1;
        assertThrows(UserNotFoundException.class, () -> {
            userService.findUserById(wrongId);
        }, "Should throw UserNotFoundException");
    }

    @Test
    void testFindByUsername_userFound() {
        User testUser = initializeTestUser();
        User foundUser = userService.findUserByUsername(testUser.getUsername());

        assertNotNull(foundUser, "Should be not null");
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertEquals(testUser.getPassword(), foundUser.getPassword());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        assertTrue(foundUser.isActive());
    }

    @Test
    void testFindByUsername_userNotFound_throwsException() {
        String username = "IncorrectUsername";
        assertThrows(UserNotFoundException.class, () -> {
            userService.findUserByUsername(username);
        }, "Should throw UserNotFoundException");

    }

    @Test
    void testFindByEmail_UserFound() {
        User testUser = initializeTestUser();
        User foundUser = userService.findUserByEmail(testUser.getEmail());

        assertNotNull(foundUser, "Should be not null");
        assertEquals(testUser.getId(), foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertEquals(testUser.getPassword(), foundUser.getPassword());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        assertTrue(foundUser.isActive());
    }

    @Test
    void testFindByEmail_userNotFound_throwsException() {
        String email = "IncorrectEmail@not.com";
        assertThrows(UserNotFoundException.class, () -> {
            userService.findUserByEmail(email);
        }, "Should throw UserNotFoundException");
    }

    @Test
    void testUpdateUserData_UpdateSucceeded() {
        User testUser = initializeTestUser();
        testUser.setUsername("ModifiedUser");
        testUser.setPassword("NewPassword123");
        testUser.setEmail("newTestMail@example.com");

        User updatedUser = userService.updateUser(testUser);

        assertNotNull(updatedUser, "Updated User should not be null");
        assertEquals(testUser.getUsername(), updatedUser.getUsername());
        assertEquals(testUser.getPassword(), updatedUser.getPassword());
        assertEquals(testUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    void testSaveNewUser_saveSucceeded() {
        User testUser = new User();
        testUser.setUsername("Tester");
        testUser.setPassword("TestPassword1");
        testUser.setEmail("tester@example.com");

        User savedUser = userService.saveNewUser(testUser);

        assertNotNull(savedUser, "User should not be null");
        assertEquals(testUser.getUsername(), savedUser.getUsername(), "Username should match");
        assertEquals(testUser.getPassword(), savedUser.getPassword(), "Password should match");
        assertEquals(testUser.getEmail(), savedUser.getEmail(), "Email should match");
        assertNull(savedUser.getToken(), "Token should be null - User not logged");
        assertFalse(savedUser.isActive(), "User not verified - Should be false");
    }

    @Test
    void testSaveNewUser_UsernameInUse_throwsException() {
        User existingUser = initializeTestUser();
        User newUser = new User();
        newUser.setUsername(existingUser.getUsername());
        newUser.setPassword("StrongPassword1");
        newUser.setEmail("testerEmail@example.com");

        assertThrows(UserAlreadyExistsException.class, () -> userService.saveNewUser(newUser),
                "User with that username already exists");
    }

    @Test
    void testSaveNewUser_EmailInUse_throwsException() {
        User existingUser = initializeTestUser();
        User newUser = new User();
        newUser.setUsername("Tester123");
        newUser.setPassword("StrongPassword1");
        newUser.setEmail(existingUser.getEmail());

        assertThrows(UserAlreadyExistsException.class, () -> userService.saveNewUser(newUser),
                "User with that email already exists");
    }

    @Test
    void testSaveNewUser_WeakPassword_throwsException() {
        User newUser = new User();
        newUser.setUsername("Tester123");
        newUser.setPassword("abc");
        newUser.setEmail("testerEmail@example.com");

        assertThrows(UnauthorizedAccessException.class, () -> userService.saveNewUser(newUser),
                "Weak password provided");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/test-UserValidation.csv")
    void testUserValidation_throwsException(String password, String token, boolean isActive, String message) {
        User testUser = initializeTestUser();
        testUser.setPassword("b8d9c3a22561f38e75b4d3e5d010973dea7caacaa8d0c6699cfd292d402bc21d");
        testUser.setToken(token);
        testUser.setActive(isActive);
        User updatedUser = userRepository.save(testUser);

        assertThrows(UnauthorizedAccessException.class, () -> {
            userService.validateUser(password, updatedUser);
        }, message);
    }

    @Test
    void testUserValidation_userValidated() {
        User testUser = initializeTestUser();
        testUser.setPassword("b8d9c3a22561f38e75b4d3e5d010973dea7caacaa8d0c6699cfd292d402bc21d");
        testUser.setToken(null);
        User updatedUser = userRepository.save(testUser);
        String correctPassword = "Password123";
        assertDoesNotThrow(() -> userService.validateUser(correctPassword, updatedUser));
    }

    @Test
    void testLogoutUser_SetTokenToNull_LogoutCompleted() {
        User testUser = initializeTestUser();

        userService.logoutUser(testUser.getId());
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();

        assertNull(updatedUser.getToken(), "Token after logout should be null");
    }

    @Test
    void testLogoutUser_UserNotFound_throwsException() {
        int notExistingId = -1;

        assertThrows(UserNotFoundException.class, () -> {
            userService.logoutUser(notExistingId);
        }, "User with negative id not exists in database. Throws exception");

    }

    @Test
    void testHashingMethod_hashesShouldBeEqual() {
        String testPassword = "password";
        String hashedPassword = "950a610738d5b022a9747ae6ede4d595ff33ec712de842319c09e83f9cb77bbc";

        assertEquals(hashedPassword, ReflectionTestUtils.invokeMethod(userService, "hashPassword", testPassword),
                "testPassword should be equal after hashing");
    }

    @ParameterizedTest()
    @CsvFileSource(resources = "/test-PasswordsCases.csv")
    void testPasswordStrength(String password, boolean expectedResult, String message) {
        boolean result = Objects.requireNonNull(ReflectionTestUtils.invokeMethod(
                userService, "checkPasswordStrength", password));

        assertEquals(expectedResult, result, message);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/test-InvalidDataInput.csv")
    void testDataValidation_throwsException(String username, String password, String email, String message) {
        User testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword(password);
        testUser.setEmail(email);

        assertThrows(InvalidDataInputException.class, () -> {
            ReflectionTestUtils.invokeMethod(
                    userService, "validateUserData", testUser);
        }, message);

    }

    private User initializeTestUser() {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("Password123");
        testUser.setEmail("test@testmail.com");
        testUser.setToken("TestToken123");
        testUser.setActive(true);
        userRepository.save(testUser);
        return testUser;
    }
}
