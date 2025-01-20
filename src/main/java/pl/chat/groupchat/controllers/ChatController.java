package pl.chat.groupchat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.requests.MessageRequest;
import pl.chat.groupchat.models.responses.MessageResponse;
import pl.chat.groupchat.services.MessageService;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final MessageService messageService;

    @Autowired
    public ChatController(MessageService messageService) {
        this.messageService = messageService;

    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        List<MessageResponse> messageResponses = messageService.getAllMessages().stream().map(MessageResponse::new)
                .toList();
        return ResponseEntity.ok(messageResponses);
    }

    @PostMapping("/send-message")
    public ResponseEntity<MessageResponse> sendMessage(@RequestHeader("Authorization") String authorization,
                                                       @RequestBody MessageRequest messageRequest) {

        Message saveMessage = messageService.saveMessage(messageRequest.getMessageBody(), messageRequest.getUserId());
        MessageResponse messageResponse = new MessageResponse(saveMessage);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);

    }
}
