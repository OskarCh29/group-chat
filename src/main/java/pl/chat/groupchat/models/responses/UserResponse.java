package pl.chat.groupchat.models.responses;

import lombok.Getter;
import lombok.Setter;
import pl.chat.groupchat.models.entities.User;

@Getter
@Setter
public class UserResponse {
    private int userId;
    private String username;
    private String token;

    public UserResponse(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.token = user.getToken();
    }
}
