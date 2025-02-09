package pl.chat.groupchat.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chat.groupchat.models.responses.MessageResponse;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.MessageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final MessageService messageService;
    private final AuthorizationService authorizationService;

    @GetMapping("/messages")
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        List<MessageResponse> messageResponses = messageService.getAllMessages().stream().map(MessageResponse::new)
                .toList();
        return ResponseEntity.ok(messageResponses);
    }
}
