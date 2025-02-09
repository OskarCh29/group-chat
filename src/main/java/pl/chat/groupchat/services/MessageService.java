package pl.chat.groupchat.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.exceptions.InvalidDataInputException;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.MessageRepository;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public Message saveMessage(String messageBody, int userId) {
        if (messageBody.trim().isEmpty()) {
            throw new InvalidDataInputException("Message empty");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        Message message = new Message();
        message.setCreatedAt(LocalDateTime.now());
        message.setMessageBody(messageBody);
        message.setUser(user);
        return messageRepository.save(message);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
