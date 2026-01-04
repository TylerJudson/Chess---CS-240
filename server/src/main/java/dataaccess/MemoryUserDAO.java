package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.AuthData;
import model.UserData;

public class MemoryUserDAO implements UserDAO {

    private Map<String, UserData> storage = new HashMap<>();
    private Map<String, AuthData> auth = new HashMap<>();

    @Override
    public void createUser(UserData user) {
        storage.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return storage.get(username);
    }

    @Override
    public void createAuth(AuthData authData) {
        auth.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return auth.get(authToken);
    }

    @Override
    public void deleteAuthData(String authToken) {
       auth.remove(authToken);
    }
}