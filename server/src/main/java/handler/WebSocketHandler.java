package handler;

import java.io.IOException;



import com.google.gson.Gson;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import org.eclipse.jetty.websocket.api.Session;

import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import requests.LeaveGameRequest;
import requests.MakeMoveRequest;
import requests.ResignGameRequest;
import service.GameService;
import service.UserService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
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
        } catch (BadRequestException ex) {
            sendError(ctx.session, "Error: " + ex.getMessage());
        } catch (UnauthorizedException ex) {
            sendError(ctx.session, "Error: " + ex.getMessage());
        } catch (ForbiddenException ex) {
            sendError(ctx.session, "Error: " + ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            sendError(ctx.session, "Error: " + ex.getMessage());
        }
    }

    private void sendError(Session session, String errorMessage) {
        try {
            ServerMessage errorMsg = new ServerMessage(ServerMessageType.ERROR, errorMessage);
            session.getRemote().sendString(gson.toJson(errorMsg));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(UserGameCommand gameCommand, Session session) throws IOException {
        // Get game data - validation happens in service
        GameData gameData = gameService.getGame(gameCommand.getGameID(), gameCommand.getAuthToken());

        connections.add(gameCommand.getGameID(), session);

        // Send LOAD_GAME to the connecting user
        LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessageType.LOAD_GAME, gameData.game());
        session.getRemote().sendString(gson.toJson(loadGameMessage));

        // Broadcast NOTIFICATION to other users
        String message = "%s joined the game.".formatted(getUserName(gameCommand.getAuthToken()));
        ServerMessage notification = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameCommand.getGameID(), session, notification);
    }
    private void leave(UserGameCommand gameCommand, Session session) throws IOException {
        gameService.leaveGame(new LeaveGameRequest(gameCommand.getGameID()), gameCommand.getAuthToken());
        String message = "%s has left the game.".formatted(getUserName(gameCommand.getAuthToken()));
        ServerMessage serverMessage = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameCommand.getGameID(), session, serverMessage);
        connections.remove(gameCommand.getGameID(), session);
    }
    private void resign(UserGameCommand gameCommand, Session session) throws IOException {
        gameService.resign(new ResignGameRequest(gameCommand.getGameID()), gameCommand.getAuthToken());
        String message = "%s has resigned the game.".formatted(getUserName(gameCommand.getAuthToken()));
        ServerMessage notification = new ServerMessage(ServerMessageType.NOTIFICATION, message);

        // Send to sender
        session.getRemote().sendString(gson.toJson(notification));

        // Broadcast to others
        connections.broadcast(gameCommand.getGameID(), session, notification);
    }

    private void makeMove(MakeMoveCommand moveCommand, Session session) throws IOException {
        GameData gameData = gameService.makeMove(new MakeMoveRequest(moveCommand.getGameID(), moveCommand.getMove()), moveCommand.getAuthToken()).gameData();
        String message = getMoveMessage(moveCommand, gameData);

        // Send LOAD_GAME to sender
        LoadGameMessage loadGame = new LoadGameMessage(ServerMessageType.LOAD_GAME, gameData.game());
        session.getRemote().sendString(gson.toJson(loadGame));

        // Broadcast LOAD_GAME to others
        connections.broadcast(moveCommand.getGameID(), session, loadGame);

        // Broadcast NOTIFICATION to others
        ServerMessage notification = new ServerMessage(ServerMessageType.NOTIFICATION, message);
        connections.broadcast(moveCommand.getGameID(), session, notification);

        if (gameData.game().isInCheckmate(gameData.game().getTeamTurn())) {
            // Send NOTIFICATION to everyone (sender and others)
            ServerMessage notification2 = new ServerMessage(ServerMessageType.NOTIFICATION, "Checkmate!");
            session.getRemote().sendString(gson.toJson(notification2));
            connections.broadcast(moveCommand.getGameID(), session, notification2);
        }
    }

    private String getUserName(String authToken) {
        AuthData authData = this.userService.getAuthData(authToken);
        if (authData != null) {
            return authData.username();
        }
        return null;
    }

    private String getMoveMessage(MakeMoveCommand moveCommand, GameData gameData) {
        String letters = "abcdefgh";
        String numbers = "12345678";

        ChessMove move = moveCommand.getMove();
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = gameData.game().getBoard().getPiece(endPosition);

        String color = piece.getTeamColor().toString();
        String pieceType = piece.getPieceType().toString();
        String startString = "" +letters.charAt(startPosition.getColumn() - 1) + numbers.charAt(startPosition.getRow() - 1);
        String endString = "" +letters.charAt(endPosition.getColumn() - 1) + numbers.charAt(endPosition.getRow() - 1);

        return "%s moved %s from %s to %s.".formatted(color, pieceType, startString, endString);
    }
}
