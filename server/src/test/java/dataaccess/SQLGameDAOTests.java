package dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import exceptions.ForbiddenException;
import model.GameData;

public class SQLGameDAOTests {
    private GameDAO gameDAO = new SQLGameDAO();
    private GameData basicGameData = new GameData(1, null, null, "gameName", new ChessGame());

    @BeforeEach
    public void setup() {
        gameDAO.clearAllData();
    }

    // CREATE GAME TESTS
    @Test
    public void createGameSuccess() {
        // Create the game
        gameDAO.createGame(basicGameData);

        // Fetch the game
        GameData fetchedGame = gameDAO.getGame(1);

        // Verify that the game is the same
        assertNotNull(fetchedGame);
        assertEquals(fetchedGame.whiteUsername(), fetchedGame.whiteUsername());
        assertEquals(fetchedGame.blackUsername(), fetchedGame.blackUsername());
        assertEquals(fetchedGame.gameName(), fetchedGame.gameName());
        assertEquals(fetchedGame.game(), fetchedGame.game());
    }

    @Test
    public void createGameFailsWithDuplicateGameID() {
        // Create the game
        gameDAO.createGame(basicGameData);

        // Create duplicate game throws forbidden
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> gameDAO.createGame(basicGameData));
        assertEquals("game name already taken", ex.getMessage());
    }

    // GET GAME TESTS
    @Test
    public void getGameSucceeds() {
        // Create the game
        gameDAO.createGame(basicGameData);

        // Fetch the game
        GameData fetchedGame = gameDAO.getGame(1);

        // Verify that the game is the same
        assertNotNull(fetchedGame);
    }

    @Test
    public void getGameFailsWithInalidGameID() {
        assertNull(gameDAO.getGame(0));
    }

    // GET ALL GAMES TESTS
    @Test
    public void getAllGamesSucceeds() {
        // Create several Games
        Collection<GameData> games = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            GameData data = new GameData(i, null, null, "game" + i, new ChessGame());
            games.add(data);
            gameDAO.createGame(data);
        }

        // Fetch all games and verify they are the same
        Collection<GameData> fetchedGames = gameDAO.getAllGames();
        assertEquals(games, fetchedGames);
    }

    @Test
    public void getAllGamesReturnsEmptyWhenNoGames() {
      Collection<GameData> games = gameDAO.getAllGames();
      assertNotNull(games);
      assertTrue(games.isEmpty());
  }

    // UPDATE GAME TESTS
    @Test
    public void updateGameSucceeds() {
        gameDAO.createGame(basicGameData);

        // Update white username
        GameData data1 = new GameData(basicGameData.gameID(), "White", null, "gameName", new ChessGame());
        gameDAO.updateGame(data1);

        // Make sure it updated properly
        assertEquals(data1, gameDAO.getGame(basicGameData.gameID()));

        // Update Black username
        GameData data2 = new GameData(basicGameData.gameID(), "White", "Black", "gameName", new ChessGame());
        gameDAO.updateGame(data2);

        // Make sure it updated properly
        assertEquals(data2, gameDAO.getGame(basicGameData.gameID()));

        // Update gameName and other props
        GameData data3 = new GameData(basicGameData.gameID(), "test1", "test2", "test3", new ChessGame());
        gameDAO.updateGame(data3);

        // Make sure it updated properly
        assertEquals(data3, gameDAO.getGame(basicGameData.gameID()));

        // Update the chess game
        ChessGame chessGame = new ChessGame();
        try {
            chessGame.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null));
        }
        catch (Exception e) {}

        GameData data4 = new GameData(basicGameData.gameID(), "test1", "test2", "test3", chessGame);
        gameDAO.updateGame(data4);

        // Make sure it updated properly
        GameData fetchedGame = gameDAO.getGame(basicGameData.gameID());
        assertEquals(data4, fetchedGame);
        assertEquals(data4.game(), fetchedGame.game());
        assertEquals(data4.game().getBoard().getPiece(new ChessPosition(2, 1)), fetchedGame.game().getBoard().getPiece(new ChessPosition(2, 1)));
    }
    
    @Test
    public void updateTestFailsInvalidID() {
        gameDAO.createGame(basicGameData);

        GameData data = new GameData(0, null, null, "gameName", new ChessGame());
        gameDAO.updateGame(data);

        assertNotEquals(data, gameDAO.getGame(basicGameData.gameID()));
    }

    // CLEAR ALL DATA TEST
    @Test
    public void clearAllDataSucceeds() {
         // Create the user
        gameDAO.createGame(basicGameData);
        
        // Verify that the user is created properly
        assertNotNull(gameDAO.getGame(basicGameData.gameID()));

        gameDAO.clearAllData();

        // Verify that the database cleared correctly
        assertNull(gameDAO.getGame(basicGameData.gameID()));
    }
}
