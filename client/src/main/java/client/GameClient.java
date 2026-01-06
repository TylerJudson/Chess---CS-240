package client;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.moveCursorToLocation;

import java.util.ArrayList;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessPosition;
import exceptions.ResponseException;
import chess.ChessGame.TeamColor;
import model.GameData;
import server.ServerFacade;
import server.ServerMessageObserver;
import server.WebSocketFacade;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

public class GameClient implements Client, ServerMessageObserver {

    Scanner scanner = new Scanner(System.in);
    ChessGame game = new ChessGame();
    ServerFacade serverFacade;
    WebSocketFacade webSocketFacade;

    TeamColor clientColor = TeamColor.WHITE;
    boolean isObserving = false;

    public GameClient(ServerFacade serverFacade, String serverUrl, String authToken, int gameId, String username) {
        this.serverFacade = serverFacade;
        
        try {
            this.webSocketFacade = new WebSocketFacade(serverUrl, this);
            this.webSocketFacade.performCommand(new UserGameCommand(CommandType.CONNECT, authToken, gameId));
        } catch (ResponseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GameData gameData = getGameData(gameId, authToken);
        if (gameData != null) {
            this.game = gameData.game();
            if (username.equals(gameData.whiteUsername())) {
                this.clientColor = TeamColor.WHITE;
            }
            else if (username.equals(gameData.blackUsername())) {
                this.clientColor = TeamColor.BLACK;
            }
            else {
                this.isObserving = true;
            }
        }
    }

    @Override
    public void notify(ServerMessage serverMessage) {
        if (serverMessage != null) {

            System.out.print(moveCursorToLocation(0, -1));

            if (serverMessage.getServerMessageType() == ServerMessageType.NOTIFICATION) {
                System.out.println(SET_TEXT_COLOR_LIGHT_GREY + serverMessage.getMessage() + RESET_TEXT_COLOR);
            }
            else if (serverMessage.getServerMessageType() == ServerMessageType.ERROR) {
                PrintUtilities.printError(serverMessage.getMessage());
            }
            else if (serverMessage.getServerMessageType() == ServerMessageType.LOAD_GAME) {
                if (serverMessage instanceof LoadGameMessage) {
                    this.game = ((LoadGameMessage)serverMessage).getGameData().game();
                    printGameBoard(null);
                }
            }

            moveCursorToLocation(0, 0);
        }
    }


    @Override
    public void help() {
        System.out.println("""
                Available Commands:
                - Move: "m" "move"            | Moves a piece.
                - Exit: "e" "exit"            | Exit the game.
                - Quit: "q" "quit"            | Quit the application.
                - Help: "h" "help"            | List available commands.
            """);
    }

    @Override
    public ClientResult eval(String str, String authToken, int gameId) {
        switch (str) {
            case "m":
            case "move":
                return move();
            case "e":
            case "exit":
                return exit(authToken, gameId);

            case "h":
            case "help":
                help();
                break;

            default:
                PrintUtilities.printError("Error: Unkown Command: '" + str + "'.");
                System.out.println("Type \"help\" to see available commands.\n");
                break;
        }

        return null;
    }

    private ClientResult move() {
        try {
            webSocketFacade.performCommand(new UserGameCommand(CommandType.LEAVE, null, null));
        } catch (ResponseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    private ClientResult exit(String authToken, int gameId) {
        try {
            UserGameCommand gameCommand = new UserGameCommand(CommandType.LEAVE, authToken, gameId);
            this.webSocketFacade.performCommand(gameCommand);
            PrintUtilities.printSuccess("Success: you have exited the game.");
            return new ClientResult(ClientType.POSTLOGIN, null, -1, null);
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return null;
    }

    public ClientResult printGameBoard(ChessPosition selectedPosition) {
        PrintUtilities.printChessBoard(this.clientColor, game, selectedPosition);
        return null;
    }



    private GameData getGameData(int gameId, String authToken) {
        try {
            ArrayList<GameData> games = serverFacade.listGames(authToken).games();
            for (GameData game : games) {
                if (game.gameID() == gameId) {
                    return game;
                }
            }
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }
        return null;
    }
}
