package pl.chat.groupchat.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.configs.AppConfig;
import pl.chat.groupchat.exceptions.UnauthorizedAccessException;
import pl.chat.groupchat.exceptions.UserAlreadyExistsException;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.exceptions.ValidateExpiredException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private static final int RESET_LINK_DURATION = 24;
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

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User saveUser(User newUser) {
        User user = userRepository.findByEmail(newUser.getEmail()).orElse(null);
        if (user != null) {
            throw new UserAlreadyExistsException("Account with this email already exists");

        } else if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with this username already exists");

        } else if (!checkPasswordStrength(newUser.getPassword())) {
            throw new UnauthorizedAccessException("Wrong password format - 6 letters, one capital, one number");
        }
        String hashPassword = hashPassword(newUser.getPassword());
        newUser.setPassword(hashPassword);

        return userRepository.save(newUser);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not Found"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with that email does not exist"));
    }

    public void validatePassword(String password, User user) {
        if (user.getPassword().equals(hashPassword(password)) && user.isActive()) {
            if (user.getToken() != null) {
                throw new UnauthorizedAccessException("Account already logged in");
            }
        } else if (!user.isActive()) {
            throw new UnauthorizedAccessException("Account not active. Please verify your e-mail");
        } else {
            throw new UnauthorizedAccessException("Wrong login or password!");
        }
    }

    private String hashPassword(String password) {
        String hashedPassword = saltPrefix + password + saltSuffix;
        return DigestUtils.sha256Hex(hashedPassword);
    }

    public void logoutUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setToken(null);
        userRepository.save(user);
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

    private boolean checkPasswordStrength(String password) {
        if (password.length() < 6) {
            return false;
        }
        return password.matches(".*\\d.*") && !password.matches(".*[A-Z].*");
    }
}
