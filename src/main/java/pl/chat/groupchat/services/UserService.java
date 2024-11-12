package pl.chat.groupchat.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;
import pl.chat.groupchat.models.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USERS_DB = "UsersDB.json";
    private Map<String, User> userMap;
    // private int nextId = 1; ogarnąć ID


    public Map<String, User> loadUsers() {
        if (userMap == null) {
            loadUsersIntoMap();
        }
        return userMap;
    }

    private List<User> loadUsersFromFile() {
        File usersFile = new File(USERS_DB);
        ObjectMapper mapper = new ObjectMapper();
        List<User> userList = new ArrayList<>();
        try {
            if (usersFile.exists()) {
                userList = mapper.readValue(usersFile, new TypeReference<List<User>>() {
                });
            }
        } catch (IOException exception) {
            System.out.println("Problem while operating with User File - IOException");
            exception.printStackTrace();
        }
        return userList.isEmpty() ? initializeAdmin(userList) : userList;

    }

    private List<User> initializeAdmin(List<User> initialList) {
        User admin = new User(0, ADMIN_USERNAME, ADMIN_PASSWORD);
        initialList.add(admin);
        saveUsersToFile(initialList);
        return initialList;
    }

    private void saveUsersToFile(List<User> usersToSave) {
        File userFile = new File(USERS_DB);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(userFile, usersToSave);
        } catch (IOException e) {
            System.out.println("Exception occurred while saving users");
        }
    }

    private Map<String, User> loadUsersIntoMap() {
        userMap = new HashMap<>();
        for (User user : loadUsersFromFile()) {
            userMap.put(user.getUsername(), user);
        }
        return userMap;
    }

    public void addUser(User newUser) {
        List<User> usersList = loadUsersFromFile();
        usersList.add(newUser);
        saveUsersToFile(usersList);

        if (userMap == null) {
            loadUsersIntoMap();
        }
        userMap.put(newUser.getUsername(), newUser);
    }

}