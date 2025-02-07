package pl.chat.groupchat.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.MessageRepository;
import pl.chat.groupchat.repositories.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class MessageServiceTests {

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
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;


    @BeforeEach
    public void clearTestDataBase() {
        messageRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void testSaveMessage_validMessage_saveSuccessfully() {

        String messageBody = "This is a test message";
        int userId = getTestUserId();

        Message savedMessage = messageService.saveMessage(messageBody, userId);

        assertNotNull(savedMessage, "Saved message should not be null");
        assertEquals(messageBody, savedMessage.getMessageBody(), "Messages should match");
        assertEquals(userId, savedMessage.getUser().getId(), "Users should match");
        assertNotNull(savedMessage.getCreatedAt(), "Should be created at the initialization");

    }

    @Test
    void testSaveMessage_UserNotFound_throwsException() {
        int wrongUserId = -1;
        String messageBody = "Test message";


        assertThrows(UserNotFoundException.class, () -> {
            messageService.saveMessage(messageBody, wrongUserId);
        }, "Should throw UserNotFoundException");
    }

    @Test
    void testGetAllMessages_GetsTwoMessages() {
        initializeTestMessage();
        List<Message> messages = messageService.getAllMessages();

        assertNotNull(messages, "Should not be null");
        assertEquals(2, messages.size(), "List size should be equal");

        assertEquals("First message", messages.get(0).getMessageBody(), "Message body should match");
        assertEquals("Second message", messages.get(1).getMessageBody(), "Message body should match");
    }

    private int getTestUserId() {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setEmail("testEmail@test.com");
        testUser.setActive(true);
        User savedUser = userRepository.save(testUser);
        return savedUser.getId();
    }

    private void initializeTestMessage() {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setEmail("testEmail@test.com");
        testUser.setActive(true);
        userRepository.save(testUser);

        Message firstMessage = new Message();
        firstMessage.setMessageBody("First message");
        firstMessage.setUser(testUser);
        messageRepository.save(firstMessage);

        Message secondMessage = new Message();
        secondMessage.setMessageBody("Second message");
        secondMessage.setUser(testUser);
        messageRepository.save(secondMessage);
    }

}
