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
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import dataaccess.UserDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.ListGamesRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.RegisterResult;


public class GameServiceTests {
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setup() {
        gameDAO = new SQLGameDAO();
        userDAO = new SQLUserDAO();
        userService = new UserService(userDAO);
        gameService = new GameService(gameDAO, userService);
        gameDAO.clearAllData();
        userDAO.clearAllData();
    }

    // CREATE GAME TESTS
    @Test
    public void createGameSuccess() {
        RegisterResult registerResult = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest, registerResult.authToken());

        assertNotNull(createGameResult.gameID());
        
        GameData createdGame = gameDAO.getGame(createGameResult.gameID());
        assertNotNull(createdGame);
        assertEquals(createdGame.gameName(), "gameName");
    }

    @Test
    public void createGameEmptyFails() {
        RegisterResult registerResult = registerBasicUser();

        List<CreateGameRequest> requests = List.of(
            new CreateGameRequest(""),
            new CreateGameRequest("    "),
            new CreateGameRequest(null),
            new CreateGameRequest("\n"),
            new CreateGameRequest("\t")
        );

        for (CreateGameRequest createGameRequest : requests) {
            BadRequestException ex = assertThrows(BadRequestException.class, () -> gameService.createGame(createGameRequest, registerResult.authToken()));
            assertEquals("bad request", ex.getMessage());
        }
    }

    @Test
    public void createGameInvalidAuthFails() {
        registerBasicUser();
        CreateGameRequest request = new CreateGameRequest("gameName");
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> gameService.createGame(request, "invalid"));
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
            CreateGameResult gameResult = gameService.createGame(new CreateGameRequest("Game" + i), registerResult.authToken());
            games2.add(gameDAO.getGame(gameResult.gameID()));
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
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest, register1Result.authToken());

        // Check joining as white works
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", createGameResult.gameID(), register1Result.authToken());
        gameService.joinGame(joinGameRequest);
        assertEquals(gameDAO.getGame(createGameResult.gameID()).whiteUsername(), "username");

        // Check joining as black works
        RegisterResult register2Result = registerBasicUser("username2");
        JoinGameRequest joinGameRequest2 = new JoinGameRequest("BLACK", createGameResult.gameID(), register2Result.authToken());
        gameService.joinGame(joinGameRequest2);
        assertEquals(gameDAO.getGame(createGameResult.gameID()).blackUsername(), "username2");
    }

    @Test
    public void joinGameAlreadyTakenWHITEFails() {
        RegisterResult register1Result = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest, register1Result.authToken());
        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", createGameResult.gameID(), register1Result.authToken());
        gameService.joinGame(joinGameRequest);

        RegisterResult register2Result = registerBasicUser("username2");
        JoinGameRequest joinGameRequest2 = new JoinGameRequest("WHITE", createGameResult.gameID(), register2Result.authToken());
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> gameService.joinGame(joinGameRequest2));
        assertEquals(ex.getMessage(), "color already taken");
    }

    @Test
    public void joinGameAlreadyTakenBLACKFails() {
        RegisterResult register1Result = registerBasicUser();
        CreateGameRequest createGameRequest = new CreateGameRequest("gameName");
        CreateGameResult createGameResult = gameService.createGame(createGameRequest, register1Result.authToken());
        JoinGameRequest joinGameRequest = new JoinGameRequest("BLACK", createGameResult.gameID(), register1Result.authToken());
        gameService.joinGame(joinGameRequest);

        RegisterResult register2Result = registerBasicUser("username2");
        JoinGameRequest joinGameRequest2 = new JoinGameRequest("BLACK", createGameResult.gameID(), register2Result.authToken());
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> gameService.joinGame(joinGameRequest2));
        assertEquals(ex.getMessage(), "color already taken");
    }

    private RegisterResult registerBasicUser() {
        return registerBasicUser("username");
    }


    private RegisterResult registerBasicUser(String username) {
        RegisterRequest request = new RegisterRequest(username, "password", "email@example.com");
        return userService.register(request);
    }
}
