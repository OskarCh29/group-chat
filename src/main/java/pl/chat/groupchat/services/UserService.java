package pl.chat.groupchat.services;

import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.User;

import java.util.HashMap;
import java.util.Map;
@Service
public class UserService {
    private Map<String, User> userDB;
    private int nextId=1;

    public int getNextId(){
        return nextId++;
    }
    public Map<String, User> initUser() {
        userDB = new HashMap<>();
        User oskar = new User(getNextId(),"Oskar","123");
        User karol = new User(getNextId(),"Diego","321");
        userDB.put(oskar.getUsername(), oskar);
        userDB.put(karol.getUsername(), karol);
        return userDB;
    }
}