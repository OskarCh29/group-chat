package pl.chat.groupchat.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.models.entities.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class AuthorizationService {
    private static final int TOKEN_LENGTH = 32;
    private static final int CODE_EXPIRY_TIME = 24;
    private final UserService userService;

    public void updateLoginToken(User user) {
        user.setToken(generateToken());
        userService.updateUser(user);

    }

    public void validateEmail(String code, User user) {
        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime codeTime = user.getVerification().getCreatedAt();
        Duration duration = Duration.between(codeTime, timeNow);
        String verificationCode = user.getVerification().getVerificationCode();
        if (duration.toHours() <= CODE_EXPIRY_TIME && code.equals(verificationCode)) {
            user.setActive(true);
            userService.updateUser(user);
        } else {
            throw new UnauthorizedAccessException("Verification code incorrect or expired");
        }

    }

    public boolean validateUserToken(String rawToken) {
        try {
            if (rawToken == null) {
                return false;
            }
            String[] tokenValues = decodeToken(rawToken);
            if (tokenValues.length != 2) {
                return false;
            }
            String userId = tokenValues[0];
            String token = tokenValues[1];
            User user = userService.findUserById(Integer.parseInt(userId));
            if (user.getToken() == null) {
                return false;
            }
            if (!user.getToken().equals(token)) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private String[] decodeToken(String rawToken) {
        try {
            byte[] decode = Base64.getDecoder().decode(rawToken);
            String decodedToken = new String(decode);
            String[] rawTokenValues = decodedToken.split(":");
            if (rawTokenValues.length != 2) {
                return new String[0];
            }
            return rawTokenValues;
        } catch (IllegalArgumentException e) {
            return new String[0];
        }
    }

    private String generateToken() {
        return RandomStringUtils.randomAlphabetic(TOKEN_LENGTH);
    }
}
