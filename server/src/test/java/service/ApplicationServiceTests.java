package service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.GameData;
import requests.CreateGameRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.RegisterResult;

public class ApplicationServiceTests {
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private GameService gameService;
    private UserService userService;
    private ApplicationService applicationService;

    @BeforeEach
    public void setup() {
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO);
        gameService = new GameService(gameDAO, userService);
        applicationService = new ApplicationService(userService, gameService);
    }

    @Test
    public void clearApplicationSuccess() {
        RegisterResult registerResult = registerBasicUser();

        // Make sure user exists before clearing
        assertNotNull(userDAO.getUser("username"));

        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest, registerResult.authToken());

        // Make sure game exists before clearing
        GameData createdGame = gameDAO.getGame(createGameResult.gameID());
        assertNotNull(createdGame);

        // Clear all data
        applicationService.clearApplication();

        // Make sure all the data is cleared
        assertNull(userDAO.getUser("username"));
        assertNull(gameDAO.getGame(createGameResult.gameID()));
    }


    private RegisterResult registerBasicUser() {
        RegisterRequest request = new RegisterRequest("username", "password", "email@example.com");
        return userService.register(request);
    }
}
