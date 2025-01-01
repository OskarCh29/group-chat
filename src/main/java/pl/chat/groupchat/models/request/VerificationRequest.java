package pl.chat.groupchat.models.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequest {
    private String verificationCode;
}
