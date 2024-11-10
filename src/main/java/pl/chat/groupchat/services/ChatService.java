package pl.chat.groupchat.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.Message;
import pl.chat.groupchat.models.User;
import pl.chat.groupchat.utils.DateTimeDeserializer;
import pl.chat.groupchat.utils.DateTimeSerializer;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class ChatService {
    @Autowired
    private MessageService messageService;
    private static final String CHAT_HISTORY = "messages.json";
    private static final String LOCK_FILE = "lock_file.lock";

    private long lastPosition = 0;
    private boolean isChatting = false;


    public void callChat(User user, Scanner scanner) throws IOException {
        File semaphore = new File(LOCK_FILE);
        if(semaphore.exists()){
            System.out.println("Another user is writing messages. Please wait");
            return;
        }
        try{
            openChat(user,scanner);
        } finally {
            semaphore.delete();
        }
    }

    public void startRefresh() {
        isChatting = true;
    }

    @Scheduled(fixedRate = 1000)
    private void loadNewMsg() throws IOException {
        if (isChatting) {
            File file = new File(CHAT_HISTORY);
            if (file.exists() && file.length() > 0) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.findAndRegisterModules();
                List<Message> messageList = mapper.readValue(file, new TypeReference<List<Message>>() {
                });
                for (int i = (int) lastPosition; i < messageList.size(); i++) {
                    Message message = messageList.get(i);
                    System.out.println(messageService.getTimeFormatted(message.getTime()) + " " +
                            message.getUsername() + ":" + message.getMessage());
                }
                lastPosition = messageList.size();
            }
        }
    }

    private void openChat(User user, Scanner scanner) throws IOException {
        System.out.println("TYPE EXIT to close chat");
        File file = new File(CHAT_HISTORY);

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addSerializer(LocalDateTime.class, new DateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new DateTimeDeserializer());
        objectMapper.registerModule(module);

        List<Message> messageList;
        if (file.exists() && file.length() > 0) {
            messageList = objectMapper.readValue(file, new TypeReference<List<Message>>() {
            });
            long maxID= messageList.stream().mapToLong(Message::getId).max().orElse(1);
            messageService.setNextId(maxID+1);

        } else {
            messageList = new ArrayList<>();
        }
        while (true) {
            String userMsg = scanner.nextLine();
            if (userMsg.equalsIgnoreCase("EXIT")) {
                System.out.println("Closing...");
                System.exit(0);
            } else {
                LocalDateTime messageTime = LocalDateTime.now();
                Message message = new Message(messageService.getNextId(), messageTime, user.getUsername(), userMsg);
                messageList.add(message);
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectMapper.writeValue(file, messageList);
            }

        }
    }
}




