package pl.chat.groupchat.services;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationServiceTests {

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private UserService userService;

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
    void testDecodeRawToken_wrongRawTokenFormat(){
        String wrongTokenFormat = "1:2:3:TooManyId";
        String rawToken = Base64.getEncoder().encodeToString(wrongTokenFormat.getBytes());

        String[] actualValues = ReflectionTestUtils.invokeMethod(authorizationService,"decodeToken",rawToken);

        assertNotNull(actualValues);
        assertEquals(0,actualValues.length,"Should return empty array");
    }

    @Test
    void testGenerateToken() {
        int tokenLength = 32;

        String token = ReflectionTestUtils.invokeMethod(authorizationService, "generateToken");

        assertNotNull(token, "Token should not be null");
        assertEquals(tokenLength, token.length(), "Generated token should have expected length");
    }
}
