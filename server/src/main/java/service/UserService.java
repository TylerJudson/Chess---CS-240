package service;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import dataaccess.SQLUserDAO;
import dataaccess.UserDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import model.AuthData;
import model.UserData;
import requests.LoginRequest;
import requests.LogoutRequest;
import requests.RegisterRequest;
import results.LoginResult;
import results.RegisterResult;

public class UserService {

    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new SQLUserDAO();
    }

    public UserService(UserDAO dao) {
        this.userDAO = dao;
    }

    public RegisterResult register(RegisterRequest registerRequest) {
        // Validate the properties of register request
        if (registerRequest.username() == null || registerRequest.username().isBlank() 
            || registerRequest.password() == null || registerRequest.password().isBlank() 
            || registerRequest.email() == null || registerRequest.email().isBlank()) {
            throw new BadRequestException("bad request");
        }

        // Check to see if username already exists
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new ForbiddenException("username already taken");
        }

        // Hash the password
        String hashedPassword = this.hashPassword(registerRequest.password());

        // Create user
        UserData user = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
        this.userDAO.createUser(user);

        AuthData authData = createAuthData(user.username());
        this.userDAO.createAuth(authData);

        return new RegisterResult(user.username(), authData.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) {
        // Validate the properties of login request
        if (loginRequest.username() == null || loginRequest.username().isBlank()
            || loginRequest.password() == null || loginRequest.password().isBlank()) {
                throw new BadRequestException("bad request");
        }

        // Verify that user exists
        UserData userData = this.userDAO.getUser(loginRequest.username());
        if (userData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // Verify password
        if (!this.verifyPassword(loginRequest.password(), userData.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        // Create auth data
        AuthData authData = createAuthData(userData.username());
        this.userDAO.createAuth(authData);

        return new LoginResult(userData.username(), authData.authToken());
    }

    public void logout(LogoutRequest logoutRequest) {
        // Verify that the authtoken is not empty
        if (logoutRequest.authtoken() == null || logoutRequest.authtoken().isBlank()) {
            throw new BadRequestException("bad request");
        }

        // Verify that the authoken is valid
        AuthData authData = this.userDAO.getAuthData(logoutRequest.authtoken());
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // Remove the authdata
        this.userDAO.deleteAuthData(authData.authToken());
    }

    public boolean isAuthorized(String authToken) {
        return !(this.userDAO.getAuthData(authToken) == null);
    }

    public AuthData getAuthData(String authToken) {
        return this.userDAO.getAuthData(authToken);
    }

    public void clearAllData() {
        this.userDAO.clearAllData();
    }
    
    private AuthData createAuthData(String username) {
        return new AuthData(UUID.randomUUID().toString(), username);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean verifyPassword(String textPassword, String hashedPassword) {
        return BCrypt.checkpw(textPassword, hashedPassword);
    }
}
