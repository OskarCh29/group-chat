package pl.chat.groupchat.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.models.entities.User;

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
        userService.updateUser(user);

    }

    public void validateUser(String rawToken) {
        try {
            if (rawToken == null) {
                throw new UnauthorizedAccessException("Access denied: Token or user missing");
            }
            String[] tokenValues = decodeToken(rawToken);
            String userId = tokenValues[0];
            String token = tokenValues[1];
            User user = userService.findUserById(Integer.parseInt(userId));
            if (!user.getToken().equals(token)) {
                throw new UnauthorizedAccessException("Access denied - token or user invalid");
            }
        } catch (NumberFormatException e) {
            throw new UnauthorizedAccessException("Access denied - user token invalid");
        }
    }

    private String[] decodeToken(String rawToken) {
        try {
            byte[] decode = Base64.getDecoder().decode(rawToken);
            String decodedToken = new String(decode);
            String[] rawTokenValues = decodedToken.split(":");
            if (rawTokenValues.length != 2) {
                throw new UnauthorizedAccessException("Access denied: Token or user invalid");
            }
            return rawTokenValues;
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Token decoding filed - Access denied");
        }
    }

    public void validateEmail(String code, User user) {
        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime codeTime = user.getVerification().getCreatedAt();
        Duration duration = Duration.between(codeTime, timeNow);
        String verificationCode = user.getVerification().getVerificationCode();
        if (duration.toHours() < CODE_EXPIRY_TIME && code.equals(verificationCode)) {
            user.setActive(true);
            userService.updateUser(user);
        } else {
            throw new UnauthorizedAccessException("Verification Code not valid or expired");
        }

    }

    private String generateToken() {
        return RandomStringUtils.randomAlphabetic(TOKEN_LENGTH);
    }
}
