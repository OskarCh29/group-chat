package pl.chat.groupchat.configs;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.chat.groupchat.repositories.UserRepository;

@Component
@Slf4j
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
            log.error(e.getMessage());
        }

    }
}
