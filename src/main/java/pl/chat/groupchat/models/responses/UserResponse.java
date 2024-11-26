package pl.chat.groupchat.models.responses;

import lombok.Getter;
import lombok.Setter;
import pl.chat.groupchat.models.entities.User;

@Getter
@Setter
public class UserResponse {
    private String userName;

    public UserResponse(User user) {
        this.userName = user.getUsername();
    }
}
