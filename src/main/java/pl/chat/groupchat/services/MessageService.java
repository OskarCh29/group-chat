package pl.chat.groupchat.services;

import org.springframework.stereotype.Service;
import pl.chat.groupchat.exceptions.InvalidMessageException;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.MessageRepository;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Message saveMessage(String messageBody, int userId) {
        if (messageBody == null || messageBody.trim().isEmpty()) {
            throw new InvalidMessageException("Empty message");
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
