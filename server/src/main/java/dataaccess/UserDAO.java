package dataaccess;

import model.AuthData;
import model.UserData;

public interface UserDAO {
    void createUser(UserData user);
    UserData getUser(String username);
    void createAuth(AuthData authData);
    AuthData getAuthData(String authToken);
    void deleteAuthData(String authToken);
}