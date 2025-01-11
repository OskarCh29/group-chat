package pl.chat.groupchat.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.exception.UnauthorizedAccessException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.request.MessageRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthorizationService {
    private static final int TOKEN_LENGTH = 32;
    private static final int CODE_EXPIRY_TIME = 24;
    private final UserService userService;

    public AuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public void updateToken(User user) {
        user.setToken(generateToken());
        userService.saveUser(user, false);

    }

    public boolean validateUser(String rawToken, MessageRequest messageRequest) throws UnauthorizedAccessException {
        if (rawToken == null || messageRequest == null) {
            throw new UnauthorizedAccessException("Access denied: Token or user missing");
        }
        byte[] decode = Base64.getDecoder().decode(rawToken);
        String decodedString = new String(decode);
        String[] requestedValues = decodedString.split(":");
        if (requestedValues.length != 2) {
            throw new UnauthorizedAccessException("Access denied: Token or user invalid");
        }
        String userId = requestedValues[0];
        String token = requestedValues[1];
        if (userId.equals(String.valueOf(messageRequest.getUserId())) && token.equals(messageRequest.getToken())) {
            return true;
        } else {
            throw new UnauthorizedAccessException("Access denied - token or user invalid");
        }
    }

    public boolean validateEmail(String code, User user) {
        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime codeTime = user.getVerification().getCreatedAt();
        Duration duration = Duration.between(codeTime, timeNow);
        String verificationCode = user.getVerification().getVerificationCode();
        if (duration.toHours() < CODE_EXPIRY_TIME && code.equals(verificationCode)) {
            user.setActive(true);
            userService.saveUser(user, false);
            return true;
        } else {
            throw new UnauthorizedAccessException("Verification Code not valid or expired");
        }

    }

    private String generateToken() {
        return RandomStringUtils.randomAlphabetic(TOKEN_LENGTH);
    }
}
