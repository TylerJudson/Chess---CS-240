package dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exceptions.ForbiddenException;
import model.AuthData;
import model.UserData;


public class SQLUserDAOTests {
    private UserDAO userDAO = new SQLUserDAO();
    private UserData basicUser = new UserData("username", "password", "email");

    @BeforeEach
    public void setup() {
        userDAO.clearAllData();
    }

    // Helper method to verify user data matches expected values
    private void verifyUserMatches(UserData expected, UserData actual) {
        assertNotNull(actual);
        assertEquals(expected.username(), actual.username());
        assertEquals(expected.password(), actual.password());
        assertEquals(expected.email(), actual.email());
    }

    // Helper method to verify auth data matches expected values
    private void verifyAuthMatches(String expectedToken, String expectedUsername, AuthData actual) {
        assertNotNull(actual);
        assertEquals(expectedToken, actual.authToken());
        assertEquals(expectedUsername, actual.username());
    }

    // Helper method to create user and auth token, returns the AuthData
    private AuthData setupUserAndAuth(String token) {
        userDAO.createUser(basicUser);
        AuthData authData = new AuthData(token, basicUser.username());
        userDAO.createAuth(authData);
        return authData;
    }

    // CREATE USER TESTS
    @Test
    public void createUserSuccess() {
        // Create the user
        userDAO.createUser(basicUser);

        // Fetch the user and verify it matches
        UserData fetched = userDAO.getUser(basicUser.username());
        verifyUserMatches(basicUser, fetched);
    }
    @Test
    public void createDuplicateUserFails() {
        // Create the user
        userDAO.createUser(basicUser);

        // Create duplicate user throws forbidden
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> userDAO.createUser(basicUser));
        assertEquals("username already taken", ex.getMessage());
    }
    
    // GET USER TESTS
    @Test
    public void getUserSuccess() {
        // Create the user
        userDAO.createUser(basicUser);

        // Fetch the user and verify it matches
        UserData fetched = userDAO.getUser(basicUser.username());
        verifyUserMatches(basicUser, fetched);
    }
    @Test
    public void getUserReturnsNullWhenUserDoesNotExist() {
        // Try to get a user that doesn't exist
        UserData result = userDAO.getUser("nonexistent");

        assertNull(result);
    }


    // CREATE AUTH TESTS
    @Test
    public void createAuthSuccess() {
        setupUserAndAuth("token");

        // Verify auth was created by fetching it
        AuthData fetched = userDAO.getAuthData("token");
        verifyAuthMatches("token", basicUser.username(), fetched);
    }

    @Test
    public void createAuthFailsForDuplicateToken() {
        userDAO.createUser(basicUser);
        AuthData basicAuth = new AuthData("token", basicUser.username());
        userDAO.createAuth(basicAuth);

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> userDAO.createAuth(basicAuth));
        assertEquals("username already taken", ex.getMessage());    
    }

    // GET AUTH TESTS
    @Test
    public void getAuthSuccess() {
        setupUserAndAuth("authToken123");

        // Test that getAuthData retrieves the correct token
        AuthData fetched = userDAO.getAuthData("authToken123");
        verifyAuthMatches("authToken123", basicUser.username(), fetched);
    }

    @Test
    public void getAuthReturnsNullInvalidAuth() {
         // Try to get an authtoken that doesn't exist
        AuthData result = userDAO.getAuthData("nonexistent");

        assertNull(result);
    }

    // DELETE AUTH DATA TESTS
    @Test
    public void deleteAuthDataSucceeds() {
        // Create user and auth token
        userDAO.createUser(basicUser);
        userDAO.createAuth(new AuthData("token", basicUser.username()));

        // Verify it created correctly
        assertNotNull(userDAO.getAuthData("token"));

        userDAO.deleteAuthData("token");
        assertNull(userDAO.getAuthData("token"));
    }
    @Test
    public void deleteAuthWithNullToken() {
        // Create user and auth token
        userDAO.createUser(basicUser);
        userDAO.createAuth(new AuthData("token", basicUser.username()));

        // Try to delete with null
        userDAO.deleteAuthData(null);

        // Original auth should still exist
        assertNotNull(userDAO.getAuthData("token"));
    }

    // CLEAR ALL DATA TEST
    @Test
    public void clearAllDataSucceeds() {
         // Create the user
        userDAO.createUser(basicUser);
        
        // Verify that the user is created properly
        assertNotNull(userDAO.getUser(basicUser.username()));

        userDAO.clearAllData();

        // Verify that the database cleared correctly
        assertNull(userDAO.getUser(basicUser.username()));
    }
}
