package pl.chat.groupchat.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.requests.ResetRequest;
import pl.chat.groupchat.models.responses.GenericResponse;
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
    public ResponseEntity<GenericResponse> verifyEmail(@PathVariable String token) {
        authorizationService.validateEmail(token, userService.findUserByEmailCode(token));
        return ResponseEntity.ok(new GenericResponse("User Verified"));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<GenericResponse> sendResetEmail(@RequestParam String email) {
        emailService.sendResetEmail(email);
        return ResponseEntity.ok(new GenericResponse("Email to reset password sent"));

    }

    @PutMapping("/resetPassword")
    public ResponseEntity<GenericResponse> resetPassword(@RequestBody @Valid ResetRequest resetRequest) {
        userService.resetPassword(resetRequest.getResetCode(), resetRequest.getNewPassword());
        return ResponseEntity.ok(new GenericResponse("Password has been updated. You can log in now"));
    }

}
