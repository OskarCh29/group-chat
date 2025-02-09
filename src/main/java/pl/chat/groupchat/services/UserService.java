package pl.chat.groupchat.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.configs.SecurityConfig;
import pl.chat.groupchat.exceptions.*;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final long RESET_LINK_DURATION = 24;
    private static final int MINIMUM_PASSWORD_LENGTH = 6;
    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;

    public User findUserById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User with that email does not exist"));
    }

    public User findUserByEmailCode(String code) {
        return userRepository.findByVerificationCode(code).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User saveNewUser(User newUser) {
        validateUserData(newUser);
        checkIfUserExists(newUser);
        String hashPassword = hashPassword(newUser.getPassword());
        newUser.setPassword(hashPassword);

        return userRepository.save(newUser);
    }

    public void validateUser(String password, User user) {
        if (!user.isActive()) {
            throw new UnauthorizedAccessException("Account not active. Please verify your email");
        }
        if (user.getToken() != null && user.getPassword().equals(hashPassword(password))) {
            throw new UnauthorizedAccessException("Account already logged in");
        }
        if (!user.getPassword().equals(hashPassword(password))) {
            throw new UnauthorizedAccessException("Wrong login or password");
        }
    }

    public void resetPassword(String resetCode, String newPassword) {
        if (!checkPasswordStrength(newPassword)) {
            throw new InvalidDataInputException("Wrong password format - 6 characters, one capital, one digit");
        }
        User user = userRepository.findByResetCode(resetCode).orElseThrow(() -> new UserNotFoundException("Invalid code"));
        Verification verification = user.getVerification();
        Duration duration = Duration.between(verification.getResetTokenCreatedAt(), LocalDateTime.now());
        if (verification.isResetUsed() || duration.toHours() >= RESET_LINK_DURATION) {
            throw new ValidationExpiredException("Reset Link used or expired");
        }
        verification.setResetUsed(true);
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    public void logoutUser(Integer userId) {
        User user = findUserById(userId);
        user.setToken(null);
        userRepository.save(user);
    }

    private String hashPassword(String password) {
        String hashedPassword = securityConfig.getSaltPrefix() + password + securityConfig.getSaltSuffix();
        return DigestUtils.sha256Hex(hashedPassword);
    }

    private boolean checkPasswordStrength(String password) {
        return password.matches(".*\\d.*") && password.matches(".*[A-Z].*")
                && password.length() >= MINIMUM_PASSWORD_LENGTH;
    }

    private void validateUserData(User user) {
        Map<String, String> userFields = new LinkedHashMap<>();
        userFields.put(user.getUsername(), "Username");
        userFields.put(user.getPassword(), "Password");
        userFields.put(user.getEmail(), "Email");
        userFields.forEach((value, field) -> {
            if (value.isBlank()) {
                throw new InvalidDataInputException(field + " field empty or contains space");
            }
        });
        if (!user.getUsername().matches("^[a-zA-z0-9]{5,}$")) {
            throw new InvalidDataInputException("Username must be 5 char length with letters at the beginning");
        }
        if (!checkPasswordStrength(user.getPassword())) {
            throw new InvalidDataInputException("Wrong password format - 6 characters, one capital, one digit");
        }
        if (!user.getEmail().matches("^[a-zA-Z0-9][a-zA-Z0-9.%+-]*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidDataInputException("Invalid email type");
        }
    }

    private void checkIfUserExists(User newUser) {
        User user = userRepository.findByEmail(newUser.getEmail()).orElse(null);
        if (user != null) {
            throw new UserAlreadyExistsException("Account with this email already exists");

        } else if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with this username already exists");
        }
    }
}
