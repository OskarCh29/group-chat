package pl.chat.groupchat.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.entities.Verification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = EmailService.class)
public class EmailServiceTests {

    @MockBean
    private UserService userService;

    @MockBean
    private JavaMailSender sender;

    @Autowired
    private EmailService emailService;


    @Test
    void testVerificationCodeGenerator() {
        int expectedLength = 10;
        String code = emailService.generateVerificationCode();

        assertEquals(expectedLength, code.length(), "Code should have expected length");
    }

    @Test
    void testSendVerificationEmail() {
        String testEmail = "TestMail@example.com";
        User testUser = new User();
        testUser.setEmail(testEmail);

        when(userService.findUserByEmail(testEmail)).thenReturn(testUser);

        emailService.sendVerificationEmail(testEmail);

        assertNotNull(testUser.getVerification(), "Verification should be updated");
        assertNotNull(testUser.getVerification().getVerificationCode(), "Code should be updated");

        verify(userService).findUserByEmail(testEmail);
        verify(sender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendResetPasswordEmail() {
        String testEmail = "TestEmail@example.com";
        User testUser = new User();
        testUser.setEmail(testEmail);
        Verification verification = new Verification();
        testUser.setVerification(verification);
        verification.setUser(testUser);

        when(userService.findUserByEmail(testEmail)).thenReturn(testUser);
        emailService.sendResetEmail(testEmail);

        assertNotNull(testUser.getVerification().getResetToken(), "Reset code applied to user");
        assertFalse(testUser.getVerification().isResetUsed(), "Link not used yet");
        assertNotNull(testUser.getVerification().getResetTokenCreatedAt(), "Time from sending saved");

        verify(userService).findUserByEmail(testEmail);
        verify(userService).updateUser(testUser);
        verify(sender).send(any(SimpleMailMessage.class));

    }

    @Test
    void testCreateUserVerification() {
        User testUser = new User();
        String verificationCode = "TestCode";

        assertNull(testUser.getVerification());

        ReflectionTestUtils.invokeMethod(emailService, "createVerification", testUser, verificationCode);

        assertNotNull(testUser.getVerification(), "User should have new verification fields");
        assertEquals(verificationCode, testUser.getVerification().getVerificationCode(), "Code applied to user");
        verify(userService).updateUser(testUser);
    }
}
