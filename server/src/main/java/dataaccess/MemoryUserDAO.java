package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.AuthData;
import model.UserData;

public class MemoryUserDAO implements UserDAO {

    private Map<String, UserData> storage = new HashMap<>();
    private Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void createUser(UserData user) {
        storage.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        UserData user = storage.get(username);
        return user != null ? user : null;
    }

    @Override
    public void createAuth(AuthData authData) {
        authTokens.put(authData.username(), authData);
    }
}