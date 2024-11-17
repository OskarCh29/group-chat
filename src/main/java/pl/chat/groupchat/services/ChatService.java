package pl.chat.groupchat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.Message;
import pl.chat.groupchat.models.User;

import java.io.*;
import java.time.LocalDateTime;
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
            try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file)) {
                String messageLine = reader.readLine();
                Message message = objectMapper.readValue(messageLine, Message.class);
                messageService.setLastID(message.getId());
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    private void loadNewMsg() throws IOException {
        if (isChatting) {
            File file = new File(CHAT_HISTORY);
            if (file.exists() && file.length() > 0) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    raf.seek(lastPosition);
                    String lineMessage;
                    while ((lineMessage = raf.readLine()) != null) {
                        Message message = objectMapper.readValue(lineMessage, Message.class);
                        log.info(messageService.getTimeFormatted(message.getTime()) + "" +
                                message.getUsername() + ": " + message.getMessageBody());
                    }
                    lastPosition = raf.getFilePointer();
                } catch (IOException e) {
                    log.error("Error reading new messages from file", e);
                }
            }
        }
    }

    private void saveMessageToFile(Message message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHAT_HISTORY, true))) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            writer.write(jsonMessage);
            writer.newLine();
        } catch (IOException e) {
            log.error("Error encountered while saving message", e);
        }
    }

    private void writeMessage(String messageBody,User user) throws IOException {
        File lockFile = new File(LOCK_FILE);
        boolean isSaving = true;
        while(isSaving){
            if(lockFile.exists()){
                lockFile.delete();
                initializeMessages();
                LocalDateTime messageTime = LocalDateTime.now();
                Message message = new Message(messageService.getNextID(),messageTime, user.getUsername(), messageBody);
                saveMessageToFile(message);
                lockFile.createNewFile();
                isSaving = false;
            }
            else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
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
                writeMessage(userMessage,user);

            }
        }
    }
}




