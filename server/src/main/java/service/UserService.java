package service;

import java.util.UUID;

import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import model.AuthData;
import model.UserData;

public class UserService {

    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new MemoryUserDAO();
    }

    public UserService(UserDAO dao) {
        this.userDAO = dao;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        // Validate the properties of register request
        if (registerRequest.username() == null || registerRequest.username().isEmpty() 
            || registerRequest.password() == null || registerRequest.password().isEmpty() 
            || registerRequest.email() == null || registerRequest.email().isEmpty()) {
            throw new BadRequestException("Bad request");
        }

        // Check to see if username already exists
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new ForbiddenException("Already taken");
        }

        // Create user
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        this.userDAO.createUser(user);

        AuthData authData = getAuthData(user.username());
        this.userDAO.createAuth(authData);

        return new RegisterResult(user.username(), authData.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) {

        return null;
    }

    public void logout(LogoutRequest logoutRequest) {

    }



    private AuthData getAuthData(String username) {
        return new AuthData(UUID.randomUUID().toString(), username);
    }
}
