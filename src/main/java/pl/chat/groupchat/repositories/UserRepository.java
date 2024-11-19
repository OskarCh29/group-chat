package pl.chat.groupchat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chat.groupchat.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByUsername(String username);
}
