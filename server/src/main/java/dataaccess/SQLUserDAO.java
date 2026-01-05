package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import exceptions.ForbiddenException;
import exceptions.ServerErrorException;

import static java.sql.Types.NULL;

import model.AuthData;
import model.UserData;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws ServerErrorException {
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) throws ServerErrorException {
        var statement = "INSERT INTO users (username, password, email, authToken) VALUES (?, ?, ?, ?)";
        this.executeQuery(statement, user.username(), user.password(), user.email(), null);
    }

    @Override
    public UserData getUser(String username) {
        var statement = "SELECT username, password, email, authToken FROM users WHERE username = ?";
        UserRow row = executeQuery(statement);
        return new UserData(row.username(), row.password(), row.email());
    }

    @Override
    public void createAuth(AuthData authData) {
        var statement = "UPDATE users SET authToken = ? WHERE username = ?";
        executeQuery(statement, authData.authToken(), authData.username());
    }

    @Override
    public AuthData getAuthData(String authToken) {
        var statement = "SELECT username, password, email, authToken FROM users WHERE authToken = ?";
        UserRow row = executeQuery(statement, authToken);
        if (row == null) {
            return null;
        }
        return new AuthData(row.authToken(), row.username());
    }

    @Override
    public void deleteAuthData(String authToken) {
        var statement = "UPDATE users SET authToken = NULL WHERE authToken = ?";
        executeQuery(statement, authToken);
    }

    @Override
    public void clearAllData() {
        executeQuery("DELETE FROM users");
    }

    private UserRow executeQuery(String statement, Object... params) throws ServerErrorException {
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new UserRow(rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("authToken"));
                }
                return null;
        }
        catch (DataAccessException | SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("already taken");
        }
        catch (SQLException e) {
            throw new ServerErrorException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createUserStatements = {
       """
        CREATE TABLE IF NOT EXISTS  users (
            `username` TEXT NOT NULL,
            `password` TEXT NOT NULL,
            `email` TEXT NOT NULL,
            `authToken` VARCHAR(255),
            PRIMARY KEY (`username`),
            UNIQUE(`authToken`)
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
