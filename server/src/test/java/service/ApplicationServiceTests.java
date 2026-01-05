package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import exceptions.UnauthorizedException;
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
        applicationService.clearApplication(new ClearApplicationRequest(registerResult.authToken()));

        // Make sure all the data is cleared
        assertNull(userDAO.getUser("username"));
        assertNull(gameDAO.getGame(createGameResult.gameData().gameID()));
    }

    @Test
    public void clearApplicationInvalidAuthFails() {
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class, 
                () -> applicationService.clearApplication(new ClearApplicationRequest("invalid")));
        assertEquals("unauthorized", ex.getMessage());
    }



    private RegisterResult registerBasicUser() {
        RegisterRequest request = new RegisterRequest("username", "password", "email@example.com");
        return userService.register(request);
    }
}
