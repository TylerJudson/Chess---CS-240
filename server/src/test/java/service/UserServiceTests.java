package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import model.UserData;

public class UserServiceTests {
    private UserDAO userDAO;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userDAO = new MemoryUserDAO();
        userService = new UserService(userDAO);
    }

    // REGISTRATION TESTS
    @Test
    public void registrationForNewUser() {
        RegisterRequest request = new RegisterRequest("Username123", "Password123", "email@test.com");
        RegisterResult result = userService.register(request);

        assertEquals("Username123", result.username());
        assertNotNull(result.authToken());

        assertNotNull(userDAO.getUser("Username123"));
        assertEquals(new UserData("Username123", "Password123", "email@test.com"), userDAO.getUser("Username123"));
    }

    @Test
    public void registrationFailsWithMissingFields() {
        RegisterRequest request1 = new RegisterRequest("", "password", "email");
        RegisterRequest request2 = new RegisterRequest("username", "", "email");
        RegisterRequest request3 = new RegisterRequest("username", "password", "");
        RegisterRequest request4 = new RegisterRequest(null, "", "email");
        RegisterRequest request5 = new RegisterRequest("", "", null);


        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> userService.register(request1));
        assertEquals("Bad request", ex1.getMessage());

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> userService.register(request2));
        assertEquals("Bad request", ex2.getMessage());

        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> userService.register(request3));
        assertEquals("Bad request", ex3.getMessage());

        BadRequestException ex4 = assertThrows(BadRequestException.class, () -> userService.register(request4));
        assertEquals("Bad request", ex4.getMessage());

        BadRequestException ex5 = assertThrows(BadRequestException.class, () -> userService.register(request5));
        assertEquals("Bad request", ex5.getMessage());
    }

    @Test
    public void registrationFailsWithExistingUser() {
        RegisterRequest request = new RegisterRequest("Username123", "Password123", "email@test.com");
        userService.register(request);

        RegisterRequest duplicateRequest = new RegisterRequest("Username123", "fd", "fd");

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> userService.register(duplicateRequest));
        assertEquals("Already taken", ex.getMessage());
    }
}
