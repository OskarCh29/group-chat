package pl.chat.groupchat.services;


import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.repositories.MessageRepository;
import pl.chat.groupchat.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Message saveMessage(String messageBody, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Message message = new Message();
        message.setCreatedAt(LocalDateTime.now());
        message.setMessageBody(messageBody);
        message.setUser(user);
        return messageRepository.save(message);
    }

    public void deleteMessage(Message message) {
        messageRepository.delete(message);
    }

    public Optional<Message> findMessageById(Long id) {
        return messageRepository.findById(id);
    }
    public List<Message> getAllMessages(){
        return messageRepository.findAll();
    }

    public String getTimeFormatted(LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return localDateTime.format(formatter);
    }


}
