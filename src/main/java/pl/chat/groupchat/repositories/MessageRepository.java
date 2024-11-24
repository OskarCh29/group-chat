package pl.chat.groupchat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.chat.groupchat.models.entities.Message;
@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {

}
