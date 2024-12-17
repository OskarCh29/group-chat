package pl.chat.groupchat.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.config.AppConfig;
import pl.chat.groupchat.exception.UserAlreadyExistsException;
import pl.chat.groupchat.exception.UserNotFoundException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final String saltPrefix;
    private final String saltSuffix;


    @Autowired
    public UserService(UserRepository userRepository, AppConfig appConfig) {
        this.userRepository = userRepository;
        this.saltPrefix = appConfig.getSaltPrefix();
        this.saltSuffix = appConfig.getSaltSuffix();
    }

    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user, boolean isNewUser) {
        if (isNewUser) {
            User newUser = userRepository.findByUsername(user.getUsername()).orElse(null);
            if (newUser != null) {
                throw new UserAlreadyExistsException("User with that username already exists");
            }
            String hashPassword = hashPassword(saltPrefix + user.getPassword() + saltSuffix);
            user.setPassword(hashPassword);
        }
        return userRepository.save(user);
    }


    public void deleteById(int userId) {
        userRepository.deleteById(userId);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User not Found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private String hashPassword(String password) {
        String hashedPassword = saltPrefix + password + saltSuffix;
        return DigestUtils.sha256Hex(hashedPassword);
    }

    public boolean validatePassword(String password, User user) {
        return user.getPassword().equals(hashPassword(password));
    }

}

