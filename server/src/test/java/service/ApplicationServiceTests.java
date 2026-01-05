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

        CreateGameRequest createGameRequest = new CreateGameRequest("gameName", registerResult.authToken());
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        // Make sure game exists before clearing
        GameData createdGame = gameDAO.getGame(createGameResult.gameData().gameID());
        assertNotNull(createdGame);

        // Clear all data
        applicationService.clearApplication();

        // Make sure all the data is cleared
        assertNull(userDAO.getUser("username"));
        assertNull(gameDAO.getGame(createGameResult.gameData().gameID()));
    }


    private RegisterResult registerBasicUser() {
        RegisterRequest request = new RegisterRequest("username", "password", "email@example.com");
        return userService.register(request);
    }
}
