package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;

import chess.ChessGame;

import static java.sql.Types.NULL;

import exceptions.ForbiddenException;
import exceptions.ServerErrorException;
import model.GameData;

public class SQLGameDAO implements GameDAO {
    Gson gson = new Gson();

    public SQLGameDAO() {
        configureDatabase();
    }

    @Override
    public void createGame(GameData gameData) {
        String statement = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, game)"
                        + "VALUES (?, ?, ?, ?, ?)";
        this.executeQuery(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
    }

    @Override
    public GameData getGame(int gameID) {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game"
                        + " FROM games WHERE gameID = ?";
        return this.executeQuery(statement, gameID);   
    }

    @Override
    public Collection<GameData> getAllGames() {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        ArrayList<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement);
            ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    games.add(new GameData(rs.getInt("gameID"), rs.getString("whiteUsername"), rs.getString("blackUsername"),
                            rs.getString("gameName"), deserializeGame(rs.getString("game"))));
                }
        }
        catch (SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("operation forbidden");
        }
        catch (DataAccessException | SQLException e) {
            throw new ServerErrorException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }

        return games;
    }

    @Override
    public void updateGame(GameData gameData) {
        String statement = "UPDATE games "
                        + "SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? "
                        + "WHERE gameID = ?";
        this.executeQuery(statement, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game(), gameData.gameID());
    }

    @Override
    public void clearAllData() {
        executeQuery("DELETE FROM games");
    }

    private GameData executeQuery(String statement, Object... params) {
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof ChessGame p) ps.setString(i + 1, serializeGame(p));
                    else if (param == null) ps.setNull(i + 1, NULL);
                }

                // Check if this is a SELECT statement
                if (statement.trim().toUpperCase().startsWith("SELECT")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return new GameData(rs.getInt("gameID"), rs.getString("whiteUsername"), rs.getString("blackUsername"),
                            rs.getString("gameName"), deserializeGame(rs.getString("game")));
                    }
                    return null;
                } else {
                    // For INSERT, UPDATE, DELETE
                    ps.executeUpdate();
                    return null;
                }
        }
        catch (SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("game name already taken");
        }
        catch (DataAccessException | SQLException e) {
            throw new ServerErrorException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private String serializeGame(ChessGame game) {
        return gson.toJson(game);
    }

    private ChessGame deserializeGame(String gameJson) {
        return gson.fromJson(gameJson, ChessGame.class);
    }

    private String[] createUserStatements = {
       """
        CREATE TABLE IF NOT EXISTS  games (
            `gameID` INT NOT NULL,
            `whiteUsername` TEXT,
            `blackUsername` TEXT,
            `gameName` TEXT NOT NULL,
            `game` TEXT,
            PRIMARY KEY (`gameID`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private void configureDatabase() {
        try {
            DatabaseManager.createDatabase();
            try (Connection conn = DatabaseManager.getConnection()) {
                for (String statement : createUserStatements) {
                    try (var preparedStatement = conn.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        }
        catch (DataAccessException | SQLException ex) {
            throw new ServerErrorException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
    
}
