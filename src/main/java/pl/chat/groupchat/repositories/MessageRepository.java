package pl.chat.groupchat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chat.groupchat.models.Message;

public interface MessageRepository extends JpaRepository<Message,Long> {

}
