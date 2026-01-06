package client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

import exceptions.ResponseException;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;
import server.Server;
import server.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static RegisterRequest basicRegisterRequest = new RegisterRequest("username", "password", "email");

    @BeforeAll
    public static void init() throws ResponseException {
        server = new Server();
        var port = server.run(0);
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void setup() throws ResponseException {
        serverFacade.clearDatabase();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    // REGISTER TESTS
    @Test
    public void registerSucceeds() throws ResponseException {
        RegisterResult result = serverFacade.register(basicRegisterRequest);
        
        assertNotNull(result);
        assertNotNull(result.authToken());
        assertEquals(result.username(), basicRegisterRequest.username());
    }

    @Test
    public void registerFailsWithInvalidUsername() {
        RegisterRequest request = new RegisterRequest("", "password", "email");
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.register(request));
        assertEquals("Error: bad request", ex.getMessage());
    }

    // LOGIN TESTS
    @Test
    public void loginSucceeds() throws ResponseException {
        serverFacade.register(basicRegisterRequest);
        LoginResult result = serverFacade.login(new LoginRequest(basicRegisterRequest.username(), basicRegisterRequest.password()));

        assertNotNull(result);
        assertNotNull(result.authToken());
        assertEquals(result.username(), basicRegisterRequest.username());
    }

    @Test
    public void loginFailsWithInvalidPassword() throws ResponseException {
        serverFacade.register(basicRegisterRequest);
        LoginRequest request = new LoginRequest(basicRegisterRequest.username(), "invalid");
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.login(request));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    // CREATE GAME TESTS
    @Test
    public void createGameSucceeds() throws ResponseException {
        RegisterResult rr = serverFacade.register(basicRegisterRequest);
        CreateGameRequest request = new CreateGameRequest("game name");
        CreateGameResult result = serverFacade.createGame(request, rr.authToken());

        assertNotNull(result);
        assertNotNull(result.gameID());
    }

    @Test
    public void createGameFailsWithInvalidAuth() throws ResponseException {
        CreateGameRequest request = new CreateGameRequest("game name");
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.createGame(request, "INVALID"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    // LIST GAME TESTS
    @Test
    public void listGamesSucceeds() throws ResponseException {
        RegisterResult rr = serverFacade.register(basicRegisterRequest);
        CreateGameResult createResult1 = serverFacade.createGame(new CreateGameRequest("game1"), rr.authToken());
        CreateGameResult createResult2 = serverFacade.createGame(new CreateGameRequest("game2"), rr.authToken());
        CreateGameResult createResult3 = serverFacade.createGame(new CreateGameRequest("game3"), rr.authToken());


        ListGamesResult result = serverFacade.listGames(rr.authToken());

        assertNotNull(result);
        assertEquals(3, result.games().size());

        // Verify that all created games are in the list with correct names
        var game1 = result.games().stream()
            .filter(g -> g.gameID() == createResult1.gameID())
            .findFirst();
        var game2 = result.games().stream()
            .filter(g -> g.gameID() == createResult2.gameID())
            .findFirst();
        var game3 = result.games().stream()
            .filter(g -> g.gameID() == createResult3.gameID())
            .findFirst();

        assertNotNull(game1.orElse(null));
        assertNotNull(game2.orElse(null));
        assertNotNull(game3.orElse(null));

        assertEquals("game1", game1.get().gameName());
        assertEquals("game2", game2.get().gameName());
        assertEquals("game3", game3.get().gameName());
    }

    // JOIN GAME TESTS
    @Test
    public void joinGameSucceeds() throws ResponseException {
        RegisterResult rr = serverFacade.register(basicRegisterRequest);
        CreateGameResult createResult = serverFacade.createGame(new CreateGameRequest("game"), rr.authToken());
        
        JoinGameRequest request = new JoinGameRequest("WHITE", createResult.gameID());
        serverFacade.joinGame(request, rr.authToken());

        ListGamesResult listResult = serverFacade.listGames(rr.authToken());

        assertEquals("username", listResult.games().getFirst().whiteUsername());
    }

    @Test
    public void joinGameFailsWithInvalidGameID() throws ResponseException {
        RegisterResult rr = serverFacade.register(basicRegisterRequest);
        serverFacade.createGame(new CreateGameRequest("game"), rr.authToken());

        JoinGameRequest request = new JoinGameRequest("WHITE", -1);
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.joinGame(request, rr.authToken()));
        assertEquals("Error: bad request", ex.getMessage());
    }

    // LOGOUT TESTS
    @Test
    public void logoutSucceeds() throws ResponseException {
        RegisterResult rr = serverFacade.register(basicRegisterRequest);

        assertNotNull(rr.authToken());

        serverFacade.logout(rr.authToken());

        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.listGames(rr.authToken()));
        assertEquals("Error: unauthorized", ex.getMessage());        
    }

    @Test
    public void logoutFailsWithInvalidAuth() throws ResponseException {
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.logout("INVALID"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    // CLEAR TEST
    @Test
    public void clearDatabaseSucceeds() throws ResponseException {
        RegisterResult rr = serverFacade.register(basicRegisterRequest);
        assertNotNull(rr);

        serverFacade.createGame(new CreateGameRequest("game name"), rr.authToken());
        ListGamesResult result = serverFacade.listGames(rr.authToken());

        assertNotNull(result);
        assertNotNull(result.games().getFirst());
        assertEquals("game name", result.games().getFirst().gameName());


        serverFacade.clearDatabase();
        
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.listGames(rr.authToken()));
        assertEquals("Error: unauthorized", ex.getMessage());
    }
    
}
