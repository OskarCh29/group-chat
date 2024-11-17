package pl.chat.groupchat.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Getter
@Setter
public class MessageService {
    public long lastID;
    private boolean isChatting = false;


    public long getNextID() {
        return ++lastID;
    }

    public String getTimeFormatted(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return localDateTime.format(formatter);
    }
}
