package pl.chat.groupchat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.request.LoginRequest;
import pl.chat.groupchat.models.responses.UserResponse;
import pl.chat.groupchat.services.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class LoginController {
    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> user = userService.findUserByUsername(loginRequest.getUsername());
        if (user.isPresent() && userService.validatePassword(loginRequest.getPassword(), user.get())) {
            UserResponse userResponse = new UserResponse(user.get());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(userResponse.getUserName());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userResponses = userService.getAllUsers().stream()
                .map(UserResponse::new).toList();
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        userService.saveUser(user);
        UserResponse userResponse = new UserResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
