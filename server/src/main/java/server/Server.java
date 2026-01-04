package server;

import java.util.Map;

import com.google.gson.Gson;

import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import handler.UserHandler;
import io.javalin.*;
import service.UserService;

public class Server {

    private final Javalin javalin;
    private Gson gson = new Gson();

    
    private UserService userService;
    private UserHandler userHandler;

    public Server() {
        this.userService = new UserService();
        this.userHandler = new UserHandler(userService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register endpoints
        javalin.post("/user", userHandler::handleRegistration);
        javalin.post("/session", userHandler::handleLogin);
        javalin.delete("/session", userHandler::handleLogout);


        // Register exceptions
        javalin.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        javalin.exception(ForbiddenException.class, (e, ctx) -> {
            ctx.status(403).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });

        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
