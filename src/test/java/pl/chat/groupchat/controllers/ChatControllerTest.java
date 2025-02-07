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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
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
    void testGetAllMessages_getOK() throws Exception {
        User testUser = new User();
        testUser.setUsername("Tester");
        Message testMessage = new Message();
        Message secondMessage = new Message();
        testMessage.setUser(testUser);
        secondMessage.setUser(testUser);
        testMessage.setMessageBody("Test");
        secondMessage.setMessageBody("Message");
        List<Message> messages = List.of(testMessage, secondMessage);

        when(messageService.getAllMessages()).thenReturn(messages);

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].messageBody").value("Test"))
                .andExpect(jsonPath("$[1].messageBody").value("Message"))
                .andExpect(jsonPath("$[0].username").value(testUser.getUsername()))
                .andExpect(jsonPath("$[1].username").value(testUser.getUsername()));
    }

    @Test
    void testSendMessage_CREATED() throws Exception {
        String messageBody = "Test message";
        int userId = 1;
        String token = "TestToken";
        MessageRequest messageRequest = new MessageRequest(messageBody);
        String header = userId + ":" + token;

        User testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("Tester");

        Message message = new Message();
        message.setMessageBody(messageBody);
        message.setUser(testUser);

        when(authorizationService.getUserIdFromHeader(anyString())).thenReturn(userId);
        when(messageService.saveMessage(messageBody, userId)).thenReturn(message);


        mockMvc.perform(post("/chat/message")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(messageRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void testSendMessage_emptyMessageBadRequest() throws Exception {
        String messageBody = " ";
        int userId = 1;
        String token = "TestToken";
        MessageRequest messageRequest = new MessageRequest(messageBody);
        String header = userId + ":" + token;

        mockMvc.perform((post("/chat/message"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", header)
                        .content(new ObjectMapper().writeValueAsString(messageRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("[\"Message empty\"]"));
    }
}


