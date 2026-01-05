package handler;

import com.google.gson.Gson;

import io.javalin.http.Context;
import service.LoginRequest;
import service.LoginResult;
import service.LogoutRequest;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

public class UserHandler {
    private UserService userService;
    Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void handleRegistration(Context ctx) {
        RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult result = this.userService.register(request);
        ctx.status(200).result(gson.toJson(result));
    }

    public void handleLogin(Context ctx) {
        LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
        LoginResult result = this.userService.login(request);
        ctx.status(200).result(gson.toJson(result));
    }

    public void handleLogout(Context ctx) {
        LogoutRequest request = new LogoutRequest(ctx.header("authorization"));
        this.userService.logout(request);
        ctx.status(200);
    }
}
