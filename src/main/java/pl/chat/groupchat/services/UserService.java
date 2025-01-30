package pl.chat.groupchat.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.configs.AppConfig;
import pl.chat.groupchat.exceptions.*;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserService {
    private static final int RESET_LINK_DURATION = 24;
    private static final int MINIMUM_PASSWORD_LENGTH = 6;
    private final UserRepository userRepository;
    private final String saltPrefix;
    private final String saltSuffix;

    @Autowired
    public UserService(UserRepository userRepository, AppConfig appConfig) {
        this.userRepository = userRepository;
        this.saltPrefix = appConfig.getSaltPrefix();
        this.saltSuffix = appConfig.getSaltSuffix();
    }

    public User findUserById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not Found"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with that email does not exist"));
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User saveNewUser(User newUser) {
        validateUserData(newUser);
        User user = userRepository.findByEmail(newUser.getEmail()).orElse(null);
        if (user != null) {
            throw new UserAlreadyExistsException("Account with this email already exists");

        } else if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with this username already exists");

        } else if (!checkPasswordStrength(newUser.getPassword())) {
            throw new UnauthorizedAccessException("Wrong password format - 6 characters, one capital, one digit");
        }
        String hashPassword = hashPassword(newUser.getPassword());
        newUser.setPassword(hashPassword);

        return userRepository.save(newUser);
    }

    public void validateUser(String password, User user) {
        if (!user.isActive()) {
            throw new UnauthorizedAccessException("Account not active. Please verify your email");
        }
        if (user.getToken() != null) {
            throw new UnauthorizedAccessException("Account already logged in");
        }
        if (!user.getPassword().equals(hashPassword(password))) {
            throw new UnauthorizedAccessException("Wrong login or password");
        }
    }

    public void resetPassword(String resetCode, String newPassword) {
        User user = userRepository.findByResetCode(resetCode).orElseThrow(() -> new UserNotFoundException("Link used"));
        Verification verification = user.getVerification();
        Duration duration = Duration.between(LocalDateTime.now(), verification.getResetTokenCreatedAt());
        if (verification.isResetUsed() || duration.toHours() > RESET_LINK_DURATION) {
            throw new ValidateExpiredException("Reset Link used or expired");
        }
        verification.setResetUsed(true);
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    public void logoutUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setToken(null);
        userRepository.save(user);
    }

    private String hashPassword(String password) {
        String hashedPassword = saltPrefix + password + saltSuffix;
        return DigestUtils.sha256Hex(hashedPassword);
    }

    private boolean checkPasswordStrength(String password) {
        return password.matches(".*\\d.*") && password.matches(".*[A-Z].*")
                && password.length() >= MINIMUM_PASSWORD_LENGTH;
    }

    private void validateUserData(User user) {
        Map<String, String> userFields = Map.of(
                user.getUsername(), "Username", user.getPassword(), "Password", user.getEmail(), "Email");
        userFields.forEach((value, field) -> {
            if (value.isBlank()) {
                throw new InvalidDataInputException(field + "field empty or contains space");
            }
        });
        if (!user.getUsername().matches("^[a-zA-z0-9]+$")) {
            throw new InvalidDataInputException("Username contains special characters");
        }
        if (!user.getEmail().matches("^[a-zA-Z0-9][a-zA-Z0-9.%+-]*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidDataInputException("Invalid email type");
        }
    }
}
