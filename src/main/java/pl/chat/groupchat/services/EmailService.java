package pl.chat.groupchat.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.exceptions.UserNotFoundException;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;
import pl.chat.groupchat.repositories.EmailRepository;

import java.time.LocalDateTime;

@Service
public class EmailService {
    private static final int VERIFICATION_CODE_LENGTH = 10;
    private final EmailRepository emailRepository;
    private final UserService userService;
    private final JavaMailSender mailSender;

    public EmailService(EmailRepository emailRepository, UserService userService, JavaMailSender mailSender) {
        this.emailRepository = emailRepository;
        this.userService = userService;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String userEmail) {
        String verifyCode = generateVerificationCode();
        String link = "http://localhost:8080/email/activate/" + verifyCode;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Activation link");
        message.setText("Click the link to activate your account: " + link);
        User user = userService.findUserByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("No such user"));
        createVerification(user, verifyCode);
        mailSender.send(message);
    }

    public String generateVerificationCode() {
        return RandomStringUtils.randomAlphabetic(VERIFICATION_CODE_LENGTH);
    }

    public User findUserByCode(String code) {
        Verification verification = emailRepository.findUserByVerificationCode(code)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return verification.getUser();

    }

    private void createVerification(User user, String code) {
        Verification refreshVerification = user.getVerification();
        if (refreshVerification != null) {
            refreshVerification.setVerificationCode(code);
            refreshVerification.setCreatedAt(LocalDateTime.now());
        }
        Verification verification = new Verification();
        verification.setVerificationCode(code);
        verification.setUser(user);
        verification.setCreatedAt(LocalDateTime.now());
        user.setVerification(verification);
        emailRepository.save(verification);
    }

    public void sendResetEmail(String email) {
        User user = userService.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("No user connected with that email"));
        String resetCode = generateVerificationCode();
        String resetLink = "http://localhost:8080/resetPassword.html?code=" + resetCode;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your link to reset password in GroupChat service");
        message.setText("Click the link to reset your password: " + resetLink);
        mailSender.send(message);

        Verification verification = user.getVerification();
        verification.setResetToken(resetCode);
        verification.setResetTokenCreatedAt(LocalDateTime.now());
        verification.setResetUsed(false);
        userService.updateUser(user);

    }
}
