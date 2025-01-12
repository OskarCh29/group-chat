package pl.chat.groupchat.models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String messageBody;
    private int userId;
    private String token;
}
