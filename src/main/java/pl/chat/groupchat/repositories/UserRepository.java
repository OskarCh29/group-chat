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
    @Query(value = "UPDATE users SET token = null", nativeQuery = true)
    void resetTokens();

    @Modifying
    @Transactional
    @Query(value = "DELETE users FROM users JOIN verification ON users.id = verification.userId "
            + "WHERE DATEDIFF(CURDATE(),verification.createdAt) >1 AND users.isActive = FALSE ", nativeQuery = true)
    void deleteInActiveUsers();

    @Query(value = "SELECT users.* FROM users JOIN verification ON users.id = verification.userId "
            + "WHERE verification.resetToken = :resetToken", nativeQuery = true)
    Optional<User> findByResetCode(String resetToken);
}
