package pl.chat.groupchat.models.responses;

import lombok.Getter;
import lombok.Setter;
import pl.chat.groupchat.models.entities.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class MessageResponse {
    private String messageBody;
    private String createdAt;
    private String username;

    public MessageResponse(Message message) {
        this.messageBody = message.getMessageBody();
        this.createdAt = message.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.username = message.getUser().getUsername();
    }
}
