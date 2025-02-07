package pl.chat.groupchat.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.entities.User;
import pl.chat.groupchat.models.requests.LoginRequest;
import pl.chat.groupchat.models.responses.GenericResponse;
import pl.chat.groupchat.models.responses.UserResponse;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.UserService;

@RestController
@RequestMapping("/api")
public class LoginController {
    private final UserService userService;
    private final AuthorizationService authorizationService;

    @Autowired
    public LoginController(UserService userService, AuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
    }
    @PostMapping("/user")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        User user = userService.findUserByUsername(loginRequest.getUsername());
        userService.validateUser(loginRequest.getPassword(), user);
        authorizationService.updateLoginToken(user);
        UserResponse userResponse = new UserResponse(user);
        return ResponseEntity.ok(userResponse);
    }
    @PutMapping("/user")
    public ResponseEntity<GenericResponse> logout(@RequestParam @Valid Integer userId) {
        userService.logoutUser(userId);
        return ResponseEntity.ok(new GenericResponse("User logged out"));
    }
}
