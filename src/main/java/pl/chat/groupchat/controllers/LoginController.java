package pl.chat.groupchat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.request.LoginRequest;
import pl.chat.groupchat.models.responses.UserResponse;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.EmailService;
import pl.chat.groupchat.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final EmailService emailService;

    @Autowired
    public LoginController(UserService userService, AuthorizationService authorizationService,
                           EmailService emailService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
        this.emailService = emailService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findUserByUsername(loginRequest.getUsername());
        userService.validatePassword(loginRequest.getPassword(), user);
        authorizationService.updateToken(user);
        UserResponse userResponse = new UserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userResponses = userService.getAllUsers().stream()
                .map(UserResponse::new).toList();
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping("/newUser")
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        userService.saveUser(user, true);
        emailService.sendVerificationEmail(user.getEmail());
        UserResponse userResponse = new UserResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
