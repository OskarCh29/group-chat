package pl.chat.groupchat.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.chat.groupchat.utils.DateTimeDeserializer;
import pl.chat.groupchat.utils.DateTimeSerializer;

import java.time.LocalDateTime;
@Getter @AllArgsConstructor
public class Message {
    private long id;
    @JsonSerialize(using = DateTimeSerializer.class)
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private LocalDateTime time;
    private String username;
    private String message;

    public Message(){}
}