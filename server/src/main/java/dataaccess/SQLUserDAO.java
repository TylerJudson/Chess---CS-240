package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import exceptions.ServerErrorException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

import model.AuthData;
import model.UserData;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws ServerErrorException, DataAccessException {
        configureDatabase();
    }

    @Override
    public void createUser(UserData user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public UserData getUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void createAuth(AuthData authData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAuth'");
    }

    @Override
    public AuthData getAuthData(String authToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAuthData'");
    }

    @Override
    public void deleteAuthData(String authToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAuthData'");
    }

    @Override
    public void clearAllData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearAllData'");
    }


    private final String[] createUserStatements = {
       """
        CREATE TABLE IF NOT EXISTS  users (
            `username` TEXT NOT NULL,
            `password` TEXT NOT NULL,
            `email` TEXT NOT NULL,
            `authToken` VARCHAR(255),
            PRIMARY KEY (`username`),
            UNIQUE(`authToken`),
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createUserStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        }
        catch (SQLException ex) {
            throw new ServerErrorException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
    
}
