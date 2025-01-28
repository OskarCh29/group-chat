package pl.chat.groupchat.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.chat.groupchat.models.entities.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.token = NULL")
    void resetTokens();

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.isActive = FALSE")
    void deleteInActiveUsers();

    @Query(value = "SELECT u FROM User u WHERE u.verification.resetToken = :resetToken")
    Optional<User> findByResetCode(String resetToken);
}