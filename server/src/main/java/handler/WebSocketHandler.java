package handler;

import java.io.IOException;



import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    UserService userService;
    GameService gameService;
    
    Gson gson = new Gson();

    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand gameCommand = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (gameCommand.getCommandType()) {
                case CONNECT -> connect(gameCommand, ctx.session);
                case LEAVE -> leave(gameCommand, ctx.session);
                case RESIGN -> resign(gameCommand, ctx.session);
                case MAKE_MOVE -> makeMove(gson.fromJson(ctx.message(), MakeMoveCommand.class), ctx.session);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(UserGameCommand gameCommand, Session session) throws IOException {
        connections.add(session);
        String message = "%s joined the game.".formatted(getUserName(gameCommand.getAuthToken()));
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, serverMessage);
    }
    private void leave(UserGameCommand gameCommand, Session session) throws IOException {
        String message = "%s has left the game.".formatted(getUserName(gameCommand.getAuthToken()));
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, serverMessage);
        connections.remove(session);
    }
    private void resign(UserGameCommand gameCommand, Session session) throws IOException {
        String message = "%s has resigned the game.".formatted(getUserName(gameCommand.getAuthToken()));
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, serverMessage);
        connections.remove(session);
    }
    private void makeMove(MakeMoveCommand moveCommand, Session session) {
        
    }

    private String getUserName(String authToken) {
        AuthData authData = this.userService.getAuthData(authToken);
        if (authData != null) {
            return authData.username();
        }
        return null;
    }
}
