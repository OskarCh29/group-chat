package pl.chat.groupchat.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class User
{
    private int id;
    private String username;
    private String password;

    public User(){}

    public boolean validatePassword(String password){
        return this.password.equals(password);
    }
}
