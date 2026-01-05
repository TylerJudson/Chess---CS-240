package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
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

        assertNotNull(createGameResult.gameData().gameID());
        
        GameData createdGame = gameDAO.getGame(createGameResult.gameData().gameID());
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



    // LIST ALL GAMES
    @Test
    public void listGamesSuccess() {
        RegisterResult registerResult = registerBasicUser();

        // no games returns an empty list
        ListGamesRequest request1 = new ListGamesRequest(registerResult.authToken());
        ListGamesResult result1 = gameService.listGames(request1);
        assertTrue(result1.games().isEmpty());
        
        Collection<GameData> games2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CreateGameResult gameResult = gameService.createGame(new CreateGameRequest("Game" + i, registerResult.authToken()));
            games2.add(gameResult.gameData());
        }

        ListGamesRequest request2 = new ListGamesRequest(registerResult.authToken());
        ListGamesResult result2 = gameService.listGames(request2);

        assertEquals(result2.games(), games2);
    }

    @Test
    public void listGamesInvalidAuthFails() {
        registerBasicUser();
        ListGamesRequest request = new ListGamesRequest("invalid");
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> gameService.listGames(request));
        assertEquals("unauthorized", ex.getMessage());
    }


    // JOIN GAME TESTS
    @Test
    public void joinGameSucess() {
        RegisterResult register1Result = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName", register1Result.authToken());
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);

        // Check joining as white works
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", createGameResult.gameData().gameID(), register1Result.authToken());
        gameService.joinGame(joinGameRequest);
        assertEquals(gameDAO.getGame(createGameResult.gameData().gameID()).whiteUsername(), "username");

        // Check joining as black works
        RegisterResult register2Result = registerBasicUser("username2");
        JoinGameRequest joinGameRequest2 = new JoinGameRequest("BLACK", createGameResult.gameData().gameID(), register2Result.authToken());
        gameService.joinGame(joinGameRequest2);
        assertEquals(gameDAO.getGame(createGameResult.gameData().gameID()).blackUsername(), "username2");
    }

    @Test
    public void joinGameAlreadyTakenWHITEFails() {
        RegisterResult register1Result = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName", register1Result.authToken());
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", createGameResult.gameData().gameID(), register1Result.authToken());
        gameService.joinGame(joinGameRequest);

        RegisterResult register2Result = registerBasicUser("username2");
        JoinGameRequest joinGameRequest2 = new JoinGameRequest("WHITE", createGameResult.gameData().gameID(), register2Result.authToken());
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> gameService.joinGame(joinGameRequest2));
        assertEquals(ex.getMessage(), "already taken");
    }

    @Test
    public void joinGameAlreadyTakenBLACKFails() {
        RegisterResult register1Result = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName", register1Result.authToken());
        CreateGameResult createGameResult = gameService.createGame(createGameRequest);
        JoinGameRequest joinGameRequest = new JoinGameRequest("BLACK", createGameResult.gameData().gameID(), register1Result.authToken());
        gameService.joinGame(joinGameRequest);

        RegisterResult register2Result = registerBasicUser("username2");
        JoinGameRequest joinGameRequest2 = new JoinGameRequest("BLACK", createGameResult.gameData().gameID(), register2Result.authToken());
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> gameService.joinGame(joinGameRequest2));
        assertEquals(ex.getMessage(), "already taken");
    }

    private RegisterResult registerBasicUser() {
        return registerBasicUser("username");
    }


    private RegisterResult registerBasicUser(String username) {
        RegisterRequest request = new RegisterRequest(username, "password", "email@example.com");
        return userService.register(request);
    }
}
