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
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    Gson gson = new Gson();

    private final ConnectionManager connections = new ConnectionManager();

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
        String message = "%s joined the game".formatted(gameCommand.getAuthToken());
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, serverMessage);
    }
    private void leave(UserGameCommand gameCommand, Session session) throws IOException {
        connections.broadcast(session, new ServerMessage(ServerMessageType.NOTIFICATION, "TESTING"));
    }
    private void resign(UserGameCommand gameCommand, Session session) {
        
    }
    private void makeMove(MakeMoveCommand moveCommand, Session session) {

    }
}
