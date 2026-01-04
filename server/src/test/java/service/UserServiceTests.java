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
import exceptions.UnauthorizedException;
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
        RegisterRequest request1 = new RegisterRequest("     ", "password", "email");
        RegisterRequest request2 = new RegisterRequest("username", "", "email");
        RegisterRequest request3 = new RegisterRequest("username", "password", "");
        RegisterRequest request4 = new RegisterRequest(null, "\n", "email");
        RegisterRequest request5 = new RegisterRequest("\t", "", null);


        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> userService.register(request1));
        assertEquals("bad request", ex1.getMessage());

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> userService.register(request2));
        assertEquals("bad request", ex2.getMessage());

        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> userService.register(request3));
        assertEquals("bad request", ex3.getMessage());

        BadRequestException ex4 = assertThrows(BadRequestException.class, () -> userService.register(request4));
        assertEquals("bad request", ex4.getMessage());

        BadRequestException ex5 = assertThrows(BadRequestException.class, () -> userService.register(request5));
        assertEquals("bad request", ex5.getMessage());
    }

    @Test
    public void registrationFailsWithExistingUser() {
        registerBasicUser();

        RegisterRequest duplicateRequest = new RegisterRequest("username", "fd", "fd");

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> userService.register(duplicateRequest));
        assertEquals("already taken", ex.getMessage());
    }


    // LOGIN TESTS
    @Test
    public void loginSuccess() {
        registerBasicUser();

        LoginRequest request = new LoginRequest("username", "password");
        LoginResult result = userService.login(request);

        assertEquals(result.username(), "username");
        assertNotNull(result.authToken());
    }

    @Test
    public void loginMissingFields() {
        LoginRequest request1 = new LoginRequest("", "");
        LoginRequest request2 = new LoginRequest("te", "");
        LoginRequest request3 = new LoginRequest("", "df");
        LoginRequest request4 = new LoginRequest("    ", "df");
        LoginRequest request5 = new LoginRequest("df", "\n");
        LoginRequest request6 = new LoginRequest("df", "\t");

        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> userService.login(request1));
        assertEquals("bad request", ex1.getMessage());

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> userService.login(request2));
        assertEquals("bad request", ex2.getMessage());

        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> userService.login(request3));
        assertEquals("bad request", ex3.getMessage());

        BadRequestException ex4 = assertThrows(BadRequestException.class, () -> userService.login(request4));
        assertEquals("bad request", ex4.getMessage());

        BadRequestException ex5 = assertThrows(BadRequestException.class, () -> userService.login(request5));
        assertEquals("bad request", ex5.getMessage());

        BadRequestException ex6 = assertThrows(BadRequestException.class, () -> userService.login(request6));
        assertEquals("bad request", ex6.getMessage());
    }

    @Test
    public void loginWithInvalidUsernmae() {
        LoginRequest request = new LoginRequest("username", "password");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> userService.login(request));
        assertEquals("unauthorized", ex.getMessage());
    }

    @Test
    public void loginWithBadPassword() {
        registerBasicUser();
        LoginRequest request = new LoginRequest("username", "wrong");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> userService.login(request));
        assertEquals("unauthorized", ex.getMessage());
    }


    private RegisterResult registerBasicUser() {
        RegisterRequest request = new RegisterRequest("username", "password", "email");
        return userService.register(request);
    }


}
