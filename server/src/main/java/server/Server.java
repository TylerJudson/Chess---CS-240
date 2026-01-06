package server;

import java.util.Map;

import com.google.gson.Gson;

import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import handler.ApplicationHandler;
import handler.GameHandler;
import handler.UserHandler;
import handler.WebSocketHandler;
import io.javalin.*;
import service.ApplicationService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;
    private Gson gson = new Gson();

    
    private UserService userService;
    private GameService gameService;
    private ApplicationService applicationService;
    private UserHandler userHandler;
    private GameHandler gameHandler;
    private ApplicationHandler applicationHandler;
    private WebSocketHandler webSocketHandler;

    public Server() {
        this.userService = new UserService();
        this.gameService = new GameService(userService);
        this.applicationService = new ApplicationService(userService, gameService);
        this.userHandler = new UserHandler(userService);
        this.gameHandler = new GameHandler(gameService);
        this.applicationHandler = new ApplicationHandler(applicationService);
        this.webSocketHandler = new WebSocketHandler(userService, gameService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register endpoints
        javalin.post("/user", this.userHandler::handleRegistration);
        javalin.post("/session", this.userHandler::handleLogin);
        javalin.delete("/session", this.userHandler::handleLogout);

        javalin.post("/game", this.gameHandler::handleCreateGame);
        javalin.get("/game", this.gameHandler::handleListGames);
        javalin.put("/game", this.gameHandler::handleJoinGames);

        javalin.delete("/db", this.applicationHandler::handleClearAppllication);

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

        // Websockets
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
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
