package pl.chat.groupchat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.User;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;


@Service
public class LoginService {
    private static final String SEMAPHORE_FILE="login_semaphore.lock";
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserService userService;

    private User login(Scanner scanner) {
        System.out.print("Login: ");
        String username = scanner.nextLine();
        System.out.print("Password:");
        String password = scanner.nextLine();

        User user = userService.initUser().get(username);
        return (user != null && user.validatePassword(password) ? user : null);
    }

    public void startChat() throws IOException {
        File file = new File(SEMAPHORE_FILE);
        if(file.exists()){
            System.out.println("Another user is logged on this account");
        }
        file.createNewFile();

        try {
            Scanner scanner = new Scanner(System.in);
            User user = login(scanner);
            if (user != null) {
                System.out.println("Login successful");
                windowSwitcher(user, scanner);
            } else {
                System.out.println("Wrong login or password");
                startChat();
            }
        }finally {
            file.delete();
        }
    }

    private void windowSwitcher(User user, Scanner scanner) {
        System.out.println("Choose next option:");
        System.out.println("1.New Message");
        System.out.println("2.Chat window");
        try {
            String string = scanner.nextLine();
            int integer = Integer.parseInt(string);
            if (integer == 1) {
                chatService.callChat(user,scanner);

            } else if (integer == 2) {
                System.out.println("Chat history:");
                chatService.startRefresh();
                
            } else {
                System.out.println("Option with this number is unavailable");
                windowSwitcher(user, scanner);
            }
        } catch (NumberFormatException | IOException e) {
            System.out.println("Wrong input");
            windowSwitcher(user, scanner);
        }
    }

}
