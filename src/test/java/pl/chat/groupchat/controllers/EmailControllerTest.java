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
import pl.chat.groupchat.exceptions.InvalidDataInputException;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.exceptions.UserAlreadyExistsException;
import pl.chat.groupchat.exceptions.ValidationExpiredException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.requests.ResetRequest;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.EmailService;
import pl.chat.groupchat.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(EmailController.class)
public class EmailControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    void testCreateNewUser_CREATED() throws Exception {
        int userId = 1;
        String username = "Tester";
        String token = "Token";
        User testUser = new User();
        testUser.setId(userId);
        testUser.setUsername(username);
        testUser.setToken(token);

        when(userService.saveNewUser(testUser)).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(anyString());

        mockMvc.perform(post("/api/newUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("userId").value(userId))
                .andExpect(jsonPath("username").value(username))
                .andExpect(jsonPath("token").value(token));
    }

    @Test
    void testCreateNewUser_UserExistsUnauthorized() throws Exception {
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("Tester");
        testUser.setToken("Token");

        when(userService.saveNewUser(any(User.class))).thenThrow(new UserAlreadyExistsException("User exists"));

        mockMvc.perform((post("/api/newUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User exists"));
    }

    @Test
    void testCreateNewUser_UserDataNotValidForbidden() throws Exception {
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("ab");
        testUser.setToken("Token");

        when(userService.saveNewUser(any(User.class))).thenThrow(
                new InvalidDataInputException("Username format invalid"));

        mockMvc.perform((post("/api/newUser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(testUser)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Username format invalid"));
    }

    @Test
    void testActivateAccount_Ok() throws Exception {
        String token = "testToken";

        doNothing().when(authorizationService).validateEmail(anyString(), any(User.class));

        mockMvc.perform((get("/api/activate/{token}", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageStatus").value("User Verified"));
    }

    @Test
    void testActivateAccount_Code() throws Exception {
        String token = "testToken";

        doThrow(new UnauthorizedAccessException("Token incorrect"))
                .when(authorizationService).validateEmail(anyString(), nullable(User.class));

        mockMvc.perform((get("/api/activate/{token}", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token incorrect"));
    }

    @Test
    void resetPassword_Ok() throws Exception {
        doNothing().when(emailService).sendResetEmail(anyString());

        mockMvc.perform((post("/api/resetPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageStatus").value("Email to reset password sent"));
    }

    @Test
    void resetPassword_passwordUpdated_Ok() throws Exception {
        String resetCode = "ResetCode";
        String newPassword = "Password123";
        ResetRequest resetRequest = new ResetRequest(resetCode, newPassword);

        doNothing().when(userService).resetPassword(anyString(), anyString());

        mockMvc.perform((put("/api/resetPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageStatus")
                        .value("Password has been updated. You can log in now"));
    }

    @Test
    void resetPassword_validationExpired_Gone() throws Exception {
        String resetCode = "ResetCode";
        String newPassword = "Password123";
        ResetRequest resetRequest = new ResetRequest(resetCode, newPassword);

        doThrow(new ValidationExpiredException("Code expired"))
                .when(userService).resetPassword(anyString(), anyString());

        mockMvc.perform((put("/api/resetPassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetRequest)))
                .andExpect(status().isGone())
                .andExpect(content().string("Code expired"));
    }
}
