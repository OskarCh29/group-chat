package pl.chat.groupchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.MessageRepository;
import pl.chat.groupchat.repositories.UserRepository;
import pl.chat.groupchat.services.MessageService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
class MessageServiceTests {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User testUser;

    @BeforeEach
    public void setUpData() {
        testUser = new User();
        testUser.setUsername("TestUser");
        testUser.setPassword("test");
        testUser.setEmail("test@tester.com");
        userRepository.save(testUser);

        Message testMessage = new Message();
        testMessage.setId(1);
        testMessage.setMessageBody("This is test message");
        testMessage.setUser(testUser);
        testMessage.setCreatedAt(LocalDateTime.now());
        messageRepository.save(testMessage);
    }

    @Test
    void testSaveMessage_validMessage_saveSuccessfully() {
        User recievedUser = userRepository.findByUsername("TestUser").orElse(null);
        assertNotNull(recievedUser);
        Message recievedMessage = messageRepository.findById(1L).orElse(null);
        assertNotNull(recievedMessage);

        assertEquals("TestUser", recievedUser.getUsername());
        assertEquals("This is test message", recievedMessage.getMessageBody());

    }

}
