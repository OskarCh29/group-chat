package pl.chat.groupchat.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.Message;
import pl.chat.groupchat.models.User;
import pl.chat.groupchat.repositories.MessageRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
@Slf4j
public class ChatService {
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private List<Message> messageHistory = new ArrayList<>();
    private boolean firstLoad = true;
    private LocalDateTime messageTimeController = null;

    @Autowired
    public ChatService(MessageRepository messageRepository, MessageService messageService) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
    }

    private boolean isChatting = false;


    public void callChat(User user, Scanner scanner) throws IOException {
        openChat(user, scanner);

    }

    public void startRefresh() {
        isChatting = true;
    }


    @Scheduled(fixedRate = 1000)
    private void loadNewMsg() {
        if (isChatting) {
            List<Message> newMessages = messageRepository.findAll();
            if (firstLoad) {
                messageHistory = newMessages;
                firstLoad = false;
                messageHistory.forEach(msg -> log.info(
                        messageService.getTimeFormatted(msg.getCreatedAt()) + " " +
                                msg.getUser().getUsername() + ": " +
                                msg.getMessageBody()));
                messageTimeController = messageHistory.getLast().getCreatedAt();
            } else {
                Message message = newMessages.getLast();
                if (message.getCreatedAt().isAfter(messageTimeController)) {
                    log.info(messageService.getTimeFormatted(message.getCreatedAt()) + " " +
                            message.getUser().getUsername() + ": " +
                            message.getMessageBody());
                    messageTimeController = message.getCreatedAt();
                }
            }
        }
    }


    private void openChat(User user, Scanner scanner) throws IOException {
        log.info("TYPE EXIT to close chat");

        while (true) {
            String userMessage = scanner.nextLine();
            if (userMessage.equalsIgnoreCase("EXIT")) {
                log.info("Closing....");
                System.exit(0);
            } else {
                messageService.saveMessage(userMessage, user.getId());

            }
        }
    }
}




