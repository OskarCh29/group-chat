package pl.chat.groupchat.configs;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import pl.chat.groupchat.repositories.UserRepository;

@Component
public class StartUpTasks {

    private final UserRepository userRepository;

    public StartUpTasks(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @PostConstruct
    public void initialize() {
        try {
            userRepository.resetTokens();
            userRepository.deleteInActiveUsers();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
