package pl.chat.groupchat.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;

import java.time.LocalDateTime;

@Service
public class EmailService {
    private static final int VERIFICATION_CODE_LENGTH = 10;
    private final JavaMailSender mailSender;
    private final UserService userService;

    @Autowired
    public EmailService(UserService userService, JavaMailSender mailSender) {
        this.userService = userService;
        this.mailSender = mailSender;
    }

    public String generateVerificationCode() {
        return RandomStringUtils.randomAlphabetic(VERIFICATION_CODE_LENGTH);
    }

    public void sendVerificationEmail(String userEmail) {
        String verifyCode = generateVerificationCode();
        String link = "http://localhost:8080/email/activate/" + verifyCode;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Activation link");
        message.setText("Click the link to activate your account: " + link);
        User user = userService.findUserByEmail(userEmail);
        createVerification(user, verifyCode);
        mailSender.send(message);
    }

    public void sendResetEmail(String email) {
        User user = userService.findUserByEmail(email);
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

    private void createVerification(User user, String code) {
        Verification verification = new Verification();
        verification.setVerificationCode(code);
        verification.setUser(user);
        verification.setCreatedAt(LocalDateTime.now());
        user.setVerification(verification);
        userService.updateUser(user);
    }
}
