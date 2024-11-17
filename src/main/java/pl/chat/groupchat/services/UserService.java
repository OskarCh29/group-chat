package pl.chat.groupchat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserService {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USERS_DB = "UsersDB.json";
    private final ObjectMapper mapper;
    @Getter
    private Map<String, User> userMap;
    private int lastID;

    public UserService(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
        this.userMap = loadUsersIntoMap();
        initializeAdmin();


    }

    private void initializeAdmin() {
        File userFile = new File(USERS_DB);
        if (!userFile.exists() || userFile.length() == 0) {
            addUser(new User(0, "Root", "123"));
            addUser(new User(1, ADMIN_USERNAME, ADMIN_PASSWORD));
        }
    }

    public void addUser(User user) {
        userMap.put(user.getUsername(), user);
        saveUsersToFile(user);
    }

    private void saveUsersToFile(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_DB, true))) {
            String userJson = mapper.writeValueAsString(user);
            writer.write(userJson);
            writer.newLine();
        } catch (IOException e) {
            log.error("Error while saving user to file", e);
        }
    }

    private Map<String, User> loadUsersIntoMap() {
        Map<String, User> userHashMap = new HashMap<>();
        File userFile = new File(USERS_DB);
        if (userFile.exists() && userFile.length() > 0) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    User user = mapper.readValue(line, User.class);
                    userHashMap.put(user.getUsername(), user);
                    if(user.getId() > lastID){
                        lastID= user.getId();
                    }
                }

            } catch (IOException e) {
                log.error("Error while loading users from file", e);
            }
        }
        return userHashMap;
    }
    public long getNextId(){
        return ++lastID;
    }
}

