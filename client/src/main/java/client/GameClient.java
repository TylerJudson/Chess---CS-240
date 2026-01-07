package client;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;

import java.util.ArrayList;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import exceptions.ResponseException;
import chess.ChessGame.TeamColor;
import model.GameData;
import server.ServerFacade;
import server.ServerMessageObserver;
import server.WebSocketFacade;
import websocket.commands.MakeMoveCommand;
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

            System.err.print("\n>> ");
        }
    }


    @Override
    public void help() {
        if (isObserving) {
            System.out.println("""
                    Available Commands:
                    - Redraw: "r" "redraw"    | Redraws the game board.
                    - Show Moves: "s" "show"  | Shows all legal moves for a piece.
                    - Exit: "e" "exit"        | Exit the game.
                    - Help: "h" "help"        | List available commands.
                """);
        }
        else {
            System.out.println("""
                    Available Commands:
                    - Move: "m" "move"        | Moves a piece.
                    - Redraw: "r" "redraw"    | Redraws the game board.
                    - Show Moves: "s" "show"  | Shows all legal moves for a piece.
                    - Exit: "e" "exit"        | Exit the game.
                    - Help: "h" "help"        | List available commands.
                """);
        }
    }

    @Override
    public ClientResult eval(String str, String authToken, int gameId) {
        switch (str) {
            case "m":
            case "move":
                if (!isObserving) {
                    return move(authToken, gameId);
                }

            case "r":
            case "redraw":
                return redraw();

            case "s":
            case "show":
                return showMoves();

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

    private ClientResult move(String authToken, int gameId) {
        System.out.print("Start ");
        ChessPosition startPosition = promptForPosition();

        System.out.print("End ");
        ChessPosition endPosition = promptForPosition();

        ChessMove move = new ChessMove(startPosition, endPosition, null);

        try {
            webSocketFacade.performCommand(new MakeMoveCommand(CommandType.MAKE_MOVE, authToken, gameId, move));
        } 
        catch (Exception e) {
            PrintUtilities.printError(e.getMessage() + ".");
        }

        return null;
    }

    private ClientResult redraw() {
        printGameBoard(null);
        return null;
    }

    private ClientResult showMoves() {
        ChessPosition position = promptForPosition();
        if (position != null) {
            if (game.getBoard().getPiece(position) != null) {
                printGameBoard(position);
            }
            else {
                PrintUtilities.printError("Error: no piece at designated position.");
            }
        }
        return null;
    }

    private ClientResult exit(String authToken, int gameId) {
        try {
            UserGameCommand gameCommand = new UserGameCommand(CommandType.LEAVE, authToken, gameId);
            this.webSocketFacade.performCommand(gameCommand);
            PrintUtilities.printSuccess("Success: you have exited the game.");
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return new ClientResult(ClientType.POSTLOGIN, null, -1, null);
    }

    public ClientResult printGameBoard(ChessPosition selectedPosition) {
        PrintUtilities.printChessBoard(this.clientColor, game, selectedPosition);
        return null;
    }

    private ChessPosition promptForPosition() {
        String letters = "abcdefgh";
        String numbers = "12345678";

        System.out.print("Position: ");
        String position = scanner.nextLine();
        if (position.length() != 2 || letters.indexOf(position.charAt(0)) == -1 || numbers.indexOf(position.charAt(1)) == -1) {
            PrintUtilities.printError("Error: invalid position.");
            System.out.println("Position must be of the form 'a1'.");
            return null;
        }

        return new ChessPosition(numbers.indexOf(position.charAt(1)) + 1, letters.indexOf(position.charAt(0)) + 1);
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
