package pl.chat.groupchat.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.Message;
import pl.chat.groupchat.models.User;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
@Slf4j
public class ChatService {
    private static final String CHAT_HISTORY = "messages.json";
    private static final String LOCK_FILE = "lock_file.lock";
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChatService(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    private long lastPosition = 0;
    private boolean isChatting = false;


    public void callChat(User user, Scanner scanner) throws IOException {
        openChat(user, scanner);

    }

    public void startRefresh() {
        isChatting = true;
    }

    private void initializeMessages() throws IOException {
        File file = new File(CHAT_HISTORY);
        if (file.exists() && file.length() > 0) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String lineMessage;
                long id = 0;
                while ((lineMessage = reader.readLine()) != null) {
                    Message message = objectMapper.readValue(lineMessage, Message.class);
                    id = Math.max(id,message.getId());
                }
                messageService.setLastID(id);
                lastPosition = file.length();
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    private void loadNewMsg() throws IOException {
        if (isChatting) {
            File file = new File(CHAT_HISTORY);
            if(file.exists() && file.length()> 0){
                try(RandomAccessFile raf = new RandomAccessFile(file,"r")){
                    raf.seek(lastPosition);
                    String lineMessage;
                    while((lineMessage = raf.readLine()) != null){
                        Message message = objectMapper.readValue(lineMessage, Message.class);
                        log.info(messageService.getTimeFormatted(message.getTime())+""+
                                message.getUsername()+": " +message.getMessageBody());
                    }
                    lastPosition = raf.getFilePointer();
                } catch (IOException e){
                    log.error("Error reading new messages from file",e);
                }
            }
        }
    }

    private void writeMessage(Message message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHAT_HISTORY, true))) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            writer.write(jsonMessage);
            writer.newLine();
        } catch (IOException e) {
            log.error("Error encountered while saving message", e);
        }
    }

    private void openChat(User user, Scanner scanner) throws IOException {
        log.info("TYPE EXIT to close chat");
        initializeMessages();
        while (true) {
            String userMessage = scanner.nextLine();
            if (userMessage.equalsIgnoreCase("EXIT")) {
                log.info("Closing....");
                System.exit(0);
            } else {
                LocalDateTime messageTime = LocalDateTime.now();
                Message message = new Message(messageService.getNextID(), messageTime, user.getUsername(), userMessage);
                writeMessage(message);
            }
        }

    }
}




