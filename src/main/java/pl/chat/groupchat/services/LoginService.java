package pl.chat.groupchat.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.User;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


@Service
@Slf4j
public class LoginService {
    private static final String SEMAPHORE_FILE = "login_semaphore_";
    private final ChatService chatService;
    private final UserService userService;

    @Autowired
    public LoginService(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    private User login(Scanner scanner) {
        log.info("Login: ");
        String username = scanner.nextLine();
        log.info("Password:");
        String password = scanner.nextLine();

        User user = userService.getUserMap().get(username);
        return (user != null && user.validatePassword(password) ? user : null);
    }

    private boolean isUserLogged(File userFile) {
        return userFile.exists();
    }

    private void initializeSemaphoreFile(File userFile) throws IOException {
        if (!userFile.exists()) {
            userFile.createNewFile();
            userFile.deleteOnExit();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (userFile.exists()) {
                    userFile.delete();
                }
            }));
        }
    }

    public void startChat() throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            User user = login(scanner);
            while (user == null) {
                log.info("Wrong login or password.");
                user = login(scanner);
            }
            File lockFile = new File(SEMAPHORE_FILE + user.getUsername() + ".lock");
            if (lockFile.exists()) {
                log.info("Another user is logged on this account");
                startChat();
            }
            initializeSemaphoreFile(lockFile);
            log.info("Login successful");
            windowSwitcher(user, scanner);
        }
    }

    private void windowSwitcher(User user, Scanner scanner) throws IOException {
        log.info("Choose next option:");
        log.info("1.New Message");
        log.info("2.Chat window");

        String option = scanner.nextLine();
        while (true) {
            if (option.equals("1")) {
                chatService.callChat(user, scanner);


            } else if (option.equals("2")) {
                log.info("Chat history:");
                chatService.startRefresh();
                break;

            } else {
                log.info("Option unavailable");
                windowSwitcher(user, scanner);
            }
        }
    }

}
