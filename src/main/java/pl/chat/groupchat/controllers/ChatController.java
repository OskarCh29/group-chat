package pl.chat.groupchat.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.requests.MessageRequest;
import pl.chat.groupchat.models.responses.MessageResponse;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.MessageService;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final MessageService messageService;
    private final AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        List<MessageResponse> messageResponses = messageService.getAllMessages().stream().map(MessageResponse::new)
                .toList();
        return ResponseEntity.ok(messageResponses);
    }

    @PostMapping("/message")
    public ResponseEntity<MessageResponse> sendMessage(@RequestHeader("Authorization") String authorization,
                                                       @Valid @RequestBody MessageRequest messageRequest) {
        int userId = authorizationService.getUserIdFromHeader(authorization);
        Message saveMessage = messageService.saveMessage(messageRequest.getMessageBody(), userId);
        MessageResponse messageResponse = new MessageResponse(saveMessage);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }
}
