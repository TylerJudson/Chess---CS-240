package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class MemoryUserDAO implements UserDAO {

    private Map<String, UserData> storage = new HashMap<>();

    @Override
    public void createUser(UserData user) {
        storage.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        UserData user = storage.get(username);
        return user != null ? user : null;
    }
}