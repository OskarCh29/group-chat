package pl.chat.groupchat.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.exception.UserNotFoundException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;
import pl.chat.groupchat.repositories.EmailRepository;

import java.time.LocalDateTime;

@Service
public class EmailService {
    private final Mailer mailer;
    private final EmailRepository emailRepository;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String username;

    public EmailService(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            EmailRepository emailRepository, UserService userService) {

        this.mailer = MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(TransportStrategy.SMTP)
                .buildMailer();
        this.emailRepository = emailRepository;
        this.userService = userService;
    }

    public void sendVerificationEmail(String userEmail) {
        User user = userService.findUserByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException("User not found"));
        createVerification(user);
        Email email = EmailBuilder.startingBlank()
                .from("GroupChat", username)
                .to(userEmail)
                .withSubject("Your account activation Code:")
                .withPlainText(user.getVerification().getVerificationCode())
                .buildEmail();
        mailer.sendMail(email);
    }


    public String generateVerificationCode() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    private void createVerification(User user) {
        String code = generateVerificationCode();
        Verification verification = new Verification();
        verification.setVerificationCode(code);
        verification.setUser(user);
        verification.setCreatedAt(LocalDateTime.now());
        user.setVerification(verification);
        emailRepository.save(verification);
    }

    public User findUserByCode(String code) {
        Verification verification = emailRepository.findUserByVerificationCode(code)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return verification.getUser();

    }

}
