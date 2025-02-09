package pl.chat.groupchat.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.requests.MessageRequest;
import pl.chat.groupchat.repositories.UserRepository;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.MessageService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void testGetAllMessages_loadSucceeded() throws Exception {
        User testUser = new User();
        testUser.setUsername("Tester");
        Message firstMessage = new Message();
        Message secondMessage = new Message();
        firstMessage.setMessageBody("First message");
        secondMessage.setMessageBody("Second message");
        firstMessage.setCreatedAt(LocalDateTime.now());
        secondMessage.setCreatedAt(LocalDateTime.now());
        firstMessage.setUser(testUser);
        secondMessage.setUser(testUser);
        List<Message> messageList = List.of(firstMessage, secondMessage);

        when(messageService.getAllMessages()).thenReturn(messageList);

        mockMvc.perform((get("/messages"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].messageBody").value("First message"))
                .andExpect(jsonPath("$.[1].messageBody").value("Second message"))
                .andExpect(jsonPath("$.[0].username").value("Tester"));

    }

    @Test
    void test_handleMessage() throws Exception {
        String messageBody = "Test message";
        int userId = 1;
        String token = "TestToken";
        MessageRequest messageRequest = new MessageRequest(messageBody);
        String header = userId + ":" + token;

        User testUser = new User();
        testUser.setUsername("Tester");

        Message message = new Message();
        message.setMessageBody(messageBody);
        message.setUser(testUser);

        when(authorizationService.getUserIdFromHeader(anyString())).thenReturn(userId);
        when(messageService.saveMessage(messageBody, userId)).thenReturn(message);

        mockMvc.perform(post("/chat")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(messageRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageBody").value(messageBody))
                .andExpect(jsonPath("$.username").value("Tester"));

        verify(authorizationService).getUserIdFromHeader(anyString());
        verify(messageService).saveMessage(messageBody, userId);
    }

    @Test
    void test_handleMessage_emptyMessageBadRequest() throws Exception {
        String messageBody = " ";
        int userId = 1;
        String token = "TestToken";
        MessageRequest messageRequest = new MessageRequest(messageBody);
        String header = userId + ":" + token;

        mockMvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", header)
                        .content(new ObjectMapper().writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"Message empty\"]"));
    }
}



