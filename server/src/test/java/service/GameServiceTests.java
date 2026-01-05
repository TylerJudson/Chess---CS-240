package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import model.GameData;


public class GameServiceTests {
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO);
        gameService = new GameService(gameDAO, userService);
    }

    // CREATE GAME TESTS
    @Test
    public void createGameSuccess() {
        RegisterResult registerResult = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName", registerResult.authToken());
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        assertNotNull(createGameResult.gameId());
        
        GameData createdGame = gameDAO.getGame(createGameResult.gameId());
        assertNotNull(createdGame);
        assertEquals(createdGame.gameName(), "gameName");
    }

    @Test
    public void createGameEmptyFails() {
        List<CreateGameRequest> requests = List.of(
            new CreateGameRequest("", ""),
            new CreateGameRequest("f", ""),
            new CreateGameRequest(null, "f"),
            new CreateGameRequest("\n", "d"),
            new CreateGameRequest("k", "   ")
        );

        for (CreateGameRequest createGameRequest : requests) {
            BadRequestException ex = assertThrows(BadRequestException.class, () -> gameService.createGame(createGameRequest));
            assertEquals("bad request", ex.getMessage());
        }
    }

    @Test
    public void createGameInvalidAuthFails() {
        registerBasicUser();
        CreateGameRequest request = new CreateGameRequest("gameName", "invalid");
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> gameService.createGame(request));
        assertEquals("unauthorized", ex.getMessage());
    }

    private RegisterResult registerBasicUser() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        return userService.register(request);
    }
}
