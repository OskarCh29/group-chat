package pl.chat.groupchat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.chat.groupchat.models.entities.Verification;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Verification, Integer> {
    Optional<Verification> findUserByVerificationCode(String code);

}
