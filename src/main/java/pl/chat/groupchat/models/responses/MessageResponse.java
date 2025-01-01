package pl.chat.groupchat.models.responses;

import lombok.Getter;
import lombok.Setter;
import pl.chat.groupchat.models.entities.Message;

import java.time.LocalDateTime;
@Getter
@Setter
public class MessageResponse {
    private String messageBody;
    private LocalDateTime createdAt;
    private String username;

    public MessageResponse(Message message) {
        this.messageBody = message.getMessageBody();
        this.createdAt = message.getCreatedAt();
        this.username = message.getUser().getUsername();
    }
}
