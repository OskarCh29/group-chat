package pl.chat.groupchat.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.requests.LoginRequest;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LoginController.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    void testLoginUser_loginOK() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUser", "Password1");
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testUser");
        testUser.setPassword("Password1");
        testUser.setToken("testToken");

        when(userService.findUserByUsername("testUser")).thenReturn(testUser);
        doNothing().when(userService).validateUser(anyString(), any(User.class));
        doNothing().when(authorizationService).updateLoginToken(testUser);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.token").value("testToken"));
    }

    @Test
    void testLoginUser_NotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUser", "Password1");

        when(userService.findUserByUsername("testUser")).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginUser_Unauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testUser", "wrongPassword");
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("CorrectPassword1");

        when(userService.findUserByUsername("testUser")).thenReturn(user);
        doThrow(new UnauthorizedAccessException("Wrong login or password"))
                .when(userService).validateUser(loginRequest.getPassword(), user);

        mockMvc.perform((post("/api/user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogoutUser_logoutOK() throws Exception {
        int userId = 1;
        User testUser = new User();
        testUser.setToken("TestToken");

        when(userService.findUserById(userId)).thenReturn(testUser);

        mockMvc.perform((put("/api/user"))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).logoutUser(userId);
    }

    @Test
    void testLogoutUser_logoutNotFound() throws Exception {
        int userId = -1;

        doThrow(new UserNotFoundException("User with that id does not exist"))
                .when(userService).logoutUser(userId);

        mockMvc.perform((put("/api/user"))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).logoutUser(userId);
    }

    @Test
    void testLogoutUser_FailedBadRequest() throws Exception {
        mockMvc.perform((put("/api/user"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testLogoutUser_ArgumentTypeMismatch() throws Exception {
        mockMvc.perform((put("/api/user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId","ABC"))
                .andExpect(status().isBadRequest());
    }
}
