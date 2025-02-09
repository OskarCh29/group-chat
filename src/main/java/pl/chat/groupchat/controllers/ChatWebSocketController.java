package pl.chat.groupchat.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import pl.chat.groupchat.models.entities.Message;
import pl.chat.groupchat.models.requests.MessageRequest;
import pl.chat.groupchat.models.responses.MessageResponse;
import pl.chat.groupchat.services.AuthorizationService;
import pl.chat.groupchat.services.MessageService;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final AuthorizationService authorizationService;
    private final MessageService messageService;

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public MessageResponse handleMessage(@Header("Authorization") String authorization,
                                         @Valid @RequestBody MessageRequest messageRequest){
        int userId = authorizationService.getUserIdFromHeader(authorization);
        Message savedMessage = messageService.saveMessage(messageRequest.getMessageBody(),userId);
        return new MessageResponse(savedMessage);
    }
}
