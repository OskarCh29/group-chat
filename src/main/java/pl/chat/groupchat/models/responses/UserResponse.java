package pl.chat.groupchat.models.responses;

import lombok.Getter;
import lombok.Setter;
import pl.chat.groupchat.models.entities.User;

@Getter
@Setter
public class UserResponse {
    private String username;
    private int userId;
    private String token;

    public UserResponse(User user) {
        this.username = user.getUsername();
        this.userId =user.getId();
        this.token = user.getToken();
    }
}
