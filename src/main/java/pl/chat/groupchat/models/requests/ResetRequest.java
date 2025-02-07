package pl.chat.groupchat.models.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Data
public class ResetRequest {

    @NotBlank(message = "Reset code missing")
    private String resetCode;

    @NotBlank(message = "New password required")
    private String newPassword;
}
