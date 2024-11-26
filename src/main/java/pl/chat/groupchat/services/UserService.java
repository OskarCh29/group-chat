package pl.chat.groupchat.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.config.AppConfig;
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

    public User saveUser(User user) {
        String hashPassword = hashPassword(saltPrefix + user.getPassword() + saltSuffix);
        user.setPassword(hashPassword);
        return userRepository.save(user);
    }

    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private String hashPassword(String password) {
        String hashedPassword = saltPrefix + password + saltSuffix;
        return DigestUtils.sha256Hex(hashedPassword);
    }

    public boolean validatePassword(String password, User user) {
        return user.getPassword().equals(hashPassword(password));
    }
}

