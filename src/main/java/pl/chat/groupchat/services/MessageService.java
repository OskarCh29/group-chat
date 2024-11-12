package pl.chat.groupchat.services;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MessageService {
    private long nextId=1;

    public void setNextId(long nextId) {
        this.nextId = nextId;
    }
    public long getNextId(){
        return nextId++;
    }

    public String getTimeFormatted(LocalDateTime localDateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return localDateTime.format(formatter);
    }

}
