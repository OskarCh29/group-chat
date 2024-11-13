package pl.chat.groupchat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.User;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


@Service
public class LoginService {
    private static final String SEMAPHORE_FILE = "login_semaphore_";
    private static final String CHATTING_SEMAPHORE = "chat_semaphore.lock";
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;

    private User login(Scanner scanner) {
        System.out.print("Login: ");
        String username = scanner.nextLine();
        System.out.print("Password:");
        String password = scanner.nextLine();

        User user = userService.loadUsers().get(username);
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
            if (user != null) {
                File lockFile = new File(SEMAPHORE_FILE + user.getUsername() + ".lock");
                if (lockFile.exists()) {
                    System.out.println("Another user is logged on this account");
                    startChat();
                }
                initializeSemaphoreFile(lockFile);
                System.out.println("Login successful");
                windowSwitcher(user, scanner);
            } else {
                System.out.println("Wrong login or password");
                startChat();
            }
        }
    }

    private void windowSwitcher(User user, Scanner scanner) throws IOException {
        System.out.println("Choose next option:");
        System.out.println("1.New Message");
        System.out.println("2.Chat window");
        File chatLock = new File(CHATTING_SEMAPHORE);
        String string = scanner.nextLine();
        if (string.equals("1")) {
            if (isUserLogged(chatLock)) {
                System.out.println("Another user is typing a message");
                windowSwitcher(user, scanner);
            } else {
                initializeSemaphoreFile(chatLock);
                chatService.callChat(user, scanner);
            }

        } else if (string.equals("2")) {
            System.out.println("Chat history:");
            chatService.startRefresh();

        } else {
            System.out.println("Option unavailable");
            windowSwitcher(user, scanner);
        }

    }

}
