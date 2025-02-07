package pl.chat.groupchat.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthorizationService.class)
public class AuthorizationServiceTests {

    @Autowired
    private AuthorizationService authorizationService;

    @MockBean
    private UserService userService;

    @Test
    void testUpdateLoginToken() {
        User user = new User();

        assertNull(user.getToken());
        authorizationService.updateLoginToken(user);

        assertNotNull(user.getToken(), "Token should be not null after update");
        assertEquals(32, user.getToken().length(), "Token length should be 32 chars");
        verify(userService).updateUser(user);

    }

    @Test
    void testValidateUserEmail_ValidationCompleted() {
        String verificationCode = "TestCode";
        User testUser = new User();
        Verification userVerification = new Verification();
        userVerification.setVerificationCode(verificationCode);
        userVerification.setCreatedAt(LocalDateTime.now().minusHours(1));

        testUser.setVerification(userVerification);
        authorizationService.validateEmail(verificationCode, testUser);

        assertTrue(testUser.isActive(), "User account is active after verification");
        verify(userService).updateUser(testUser);
    }

    @Test
    void testValidateUserEmail_ValidationCodeExpired() {
        String verificationCode = "TestCode";
        User testUser = new User();
        Verification userVerification = new Verification();
        userVerification.setVerificationCode(verificationCode);
        userVerification.setCreatedAt(LocalDateTime.now().minusHours(25));

        testUser.setVerification(userVerification);
        userVerification.setUser(testUser);

        assertThrows(UnauthorizedAccessException.class, () -> {
            authorizationService.validateEmail(verificationCode, testUser);
        }, "Verification code is expired after 24h");
    }

    @Test
    void testValidateUserEmail_ValidationCodeNotCorrect() {
        String incorrectCode = "TestCode";
        User testUser = new User();
        Verification userVerification = new Verification();
        userVerification.setVerificationCode("CorrectCode");
        userVerification.setCreatedAt(LocalDateTime.now());
        testUser.setVerification(userVerification);
        userVerification.setUser(testUser);

        assertThrows(UnauthorizedAccessException.class, () -> {
            authorizationService.validateEmail(incorrectCode, testUser);
        }, "Incorrect verification code");


    }

    @Test
    void testValidateUserToken_userValidated() {
        String token = "1:Test";
        String rawToken = Base64.getEncoder().encodeToString(token.getBytes());
        User testUser = new User();
        testUser.setToken("Test");

        when(userService.findUserById(1)).thenReturn(testUser);

        boolean result = authorizationService.validateUserToken(rawToken);
        assertTrue(result, "Tokens equals user validated");
        verify(userService).findUserById(1);
    }

    @Test
    void testValidateUserToken_rawTokenAsNull() {
        boolean result = authorizationService.validateUserToken(null);
        assertFalse(result, "Should return false - User not validated");
    }

    @Test
    void testValidateUserToken_wrongTokenFormat() {
        String wrongToken = "1:2:InvalidToken";
        String rawToken = Base64.getEncoder().encodeToString(wrongToken.getBytes());

        boolean result = authorizationService.validateUserToken(rawToken);
        assertFalse(result, "Token after decoding should return false based on requirements");
    }

    @Test
    void testValidateUserToken_userNotValidated() {
        String token = "1:TestToken";
        String rawToken = Base64.getEncoder().encodeToString(token.getBytes());
        User testUser = new User();
        when(userService.findUserById(1)).thenReturn(testUser);

        boolean result = authorizationService.validateUserToken(rawToken);
        assertFalse(result, "User has no token");
        verify(userService).findUserById(1);
    }

    @Test
    void testValidateUserToken_noIdProvidedInToken() {
        String noIdToken = "1:NoIdToken";
        String rawToken = Base64.getEncoder().encodeToString(noIdToken.getBytes());

        when(userService.findUserById(1)).thenThrow(NumberFormatException.class);

        boolean result = authorizationService.validateUserToken(rawToken);
        assertFalse(result, "No id provided - Failed parsing to int");
        verify(userService).findUserById(1);
    }

    @Test
    void testValidateUserToken_TokensNotEqual() {
        String token = "1:IncorrectToken";
        String rawToken = Base64.getEncoder().encodeToString(token.getBytes());
        User testUser = new User();
        testUser.setToken("CorrectToken");

        when(userService.findUserById(1)).thenReturn(testUser);

        boolean result = authorizationService.validateUserToken(rawToken);
        assertFalse(result, "User token not equal with received");
    }
    @Test
    void testGetUserIdFromHeader(){
        String values = "1:Token";
        String header = Base64.getEncoder().encodeToString(values.getBytes());
        int expectedId = 1;

        int userId =authorizationService.getUserIdFromHeader(header);

        assertEquals(expectedId, userId);
    }

    @Test
    void testDecodeRawToken_decodingSucceed() {
        String userIdWithToken = "1:TestToken";
        String rawToken = Base64.getEncoder().encodeToString(userIdWithToken.getBytes());
        String[] expectedValues = {"1", "TestToken"};

        String[] actualValues = ReflectionTestUtils.invokeMethod(authorizationService, "decodeToken", rawToken);

        assertArrayEquals(expectedValues, actualValues, "Values after decoding should be as expected");
    }

    @Test
    void testDecodeRawToken_inValidInput() {
        String rawToken = "1@B";

        String[] actualValues = ReflectionTestUtils.invokeMethod(authorizationService, "decodeToken", rawToken);

        assertNotNull(actualValues);
        assertEquals(0, actualValues.length, "Should return empty array");
    }

    @Test
    void testDecodeRawToken_wrongRawTokenFormat() {
        String wrongTokenFormat = "1:2:3:TooManyId";
        String rawToken = Base64.getEncoder().encodeToString(wrongTokenFormat.getBytes());

        String[] actualValues = ReflectionTestUtils.invokeMethod(authorizationService, "decodeToken", rawToken);

        assertNotNull(actualValues);
        assertEquals(0, actualValues.length, "Should return empty array");
    }

    @Test
    void testGenerateToken() {
        int tokenLength = 32;

        String token = ReflectionTestUtils.invokeMethod(authorizationService, "generateToken");

        assertNotNull(token, "Token should not be null");
        assertEquals(tokenLength, token.length(), "Generated token should have expected length");
    }
}
