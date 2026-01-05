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

    public SQLUserDAO() {
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) {
        String statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        this.executeUserQuery(statement, user.username(), user.password(), user.email());
    }

    @Override
    public UserData getUser(String username) {
        String statement = "SELECT username, password, email FROM users WHERE username = ?";
        return executeUserQuery(statement, username);
    }

    @Override
    public void createAuth(AuthData authData) {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        executeUserQuery(statement, authData.authToken(), authData.username());
    }

    @Override
    public AuthData getAuthData(String authToken) {
        String statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
        return executeAuthQuery(statement, authToken);
    }

    @Override
    public void deleteAuthData(String authToken) {
        String statement = "DELETE FROM auth WHERE authToken = ?";
        executeUserQuery(statement, authToken);
    }

    @Override
    public void clearAllData() {
        executeUserQuery("DELETE FROM users");
        executeAuthQuery("DELETE FROM auth");
    }

    private UserData executeUserQuery(String statement, Object... params) {
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }

                // Check if this is a SELECT statement
                if (statement.trim().toUpperCase().startsWith("SELECT")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    }
                    return null;
                } else {
                    // For INSERT, UPDATE, DELETE
                    ps.executeUpdate();
                    return null;
                }
        }
        catch (SQLIntegrityConstraintViolationException e) {
            throw new ForbiddenException("already taken");
        }
        catch (DataAccessException | SQLException e) {
            throw new ServerErrorException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private AuthData executeAuthQuery(String statement, Object... params) {
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }

                // Check if this is a SELECT statement
                if (statement.trim().toUpperCase().startsWith("SELECT")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"), rs.getString("username"));
                    }
                    return null;
                } else {
                    // For INSERT, UPDATE, DELETE
                    ps.executeUpdate();
                    return null;
                }
        }
        catch (DataAccessException | SQLException e) {
            throw new ServerErrorException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private String[] createUserStatements = {
       """
        CREATE TABLE IF NOT EXISTS  users (
            `username` VARCHAR(255) NOT NULL,
            `password` TEXT NOT NULL,
            `email` VARCHAR(255) NOT NULL,
            PRIMARY KEY (`username`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private String[] createAuthStatements = {
        """
        CREATE TABLE IF NOT EXISTS  auth (
            `authToken` VARCHAR(255) NOT NULL,
            `username` VARCHAR(255) NOT NULL,
            PRIMARY KEY (`authToken`)
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
                for (String statement : createAuthStatements) {
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
