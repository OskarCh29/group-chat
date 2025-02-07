package pl.chat.groupchat.models.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Data
public class MessageRequest {

    @NotBlank(message = "Message empty")
    private String messageBody;

    @NotNull(message = "userId not provided")
    private int userId;

    @NotBlank(message = "User verification token missing")
    private String token;
}
