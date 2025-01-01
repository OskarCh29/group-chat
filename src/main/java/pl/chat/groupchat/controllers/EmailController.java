package pl.chat.groupchat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chat.groupchat.models.request.VerificationRequest;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.EmailService;

@RestController
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;
    private final AuthorizationService authorizationService;

    @Autowired
    EmailController(EmailService emailService, AuthorizationService authorizationService) {
        this.emailService = emailService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestBody VerificationRequest verificationRequest) {
        String code = verificationRequest.getVerificationCode();
        if (authorizationService.validateEmail(
                code, emailService.findUserByCode(code))
        )
        {
            return ResponseEntity.ok("User Verified");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}
