package server;

import handler.UserHandler;
import io.javalin.*;
import service.UserService;

public class Server {

    private final Javalin javalin;
    
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

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
