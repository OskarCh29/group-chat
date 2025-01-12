package pl.chat.groupchat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.requests.ResetRequest;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.EmailService;
import pl.chat.groupchat.services.UserService;

@RestController
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;
    private final AuthorizationService authorizationService;
    private final UserService userService;

    @Autowired
    EmailController(EmailService emailService, AuthorizationService authorizationService, UserService userService) {
        this.emailService = emailService;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    @GetMapping("/activate/{token}")
    public ResponseEntity<String> verifyEmail(@PathVariable String token) {
        authorizationService.validateEmail(token, emailService.findUserByCode(token));
        return ResponseEntity.ok("User Verified");
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> sendResetEmail(@RequestParam String email) {
        emailService.sendResetEmail(email);
        return ResponseEntity.ok("Email to reset password sent");

    }

    @PutMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody ResetRequest resetRequest) {
        userService.resetPassword(resetRequest.getResetCode(), resetRequest.getNewPassword());
        return ResponseEntity.ok("Password has been updated. You can log in now");
    }

}
