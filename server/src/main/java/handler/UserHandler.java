package handler;

import io.javalin.http.Context;
import service.UserService;

public class UserHandler {
    private UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void handleRegistration(Context ctx) {

    }

    public void handleLogin(Context ctx) {

    }

    public void handleLogout(Context ctx) {

    }
}
