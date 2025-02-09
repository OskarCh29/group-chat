package pl.chat.groupchat.configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartUpTasks {

    private final UserRepository userRepository;

    @PostConstruct
    public void initialize() {
        try {
            LocalDateTime inActiveTime = LocalDateTime.now();
            userRepository.resetTokens();
            userRepository.deleteInActiveUsers(inActiveTime);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
