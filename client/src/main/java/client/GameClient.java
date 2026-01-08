package client;

import java.util.ArrayList;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import exceptions.ResponseException;
import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;
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

    String message;
    boolean showHelp;

    TeamColor clientColor = TeamColor.WHITE;
    boolean isObserving = false;

    public GameClient(ServerFacade serverFacade, String serverUrl, String authToken, int gameId, String username) {
        this.serverFacade = serverFacade;

        PrintUtilities.clearScreen();

        GameData gameData = getGameData(gameId, authToken);
        if (gameData != null) {
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
        
        try {
            this.webSocketFacade = new WebSocketFacade(serverUrl, this);
            this.webSocketFacade.performCommand(new UserGameCommand(CommandType.CONNECT, authToken, gameId));
        } catch (ResponseException e) {
            PrintUtilities.printError(e.getMessage() + '.');
        }

        
    }

    @Override
    public void notify(ServerMessage serverMessage) {
        if (serverMessage != null) {
            if (serverMessage.getServerMessageType() == ServerMessageType.NOTIFICATION) {
                message = serverMessage.getMessage();
                redraw();
            }
            else if (serverMessage.getServerMessageType() == ServerMessageType.ERROR) {
                redraw();
                PrintUtilities.printError(serverMessage.getErrorMessage() + ".");
            }
            else if (serverMessage.getServerMessageType() == ServerMessageType.LOAD_GAME) {
                if (serverMessage instanceof LoadGameMessage) {
                    message = serverMessage.getMessage();
                    this.game = ((LoadGameMessage)serverMessage).getGame();
                }
                redraw();
            }
            System.out.print(">> ");
        }
    }


    @Override
    public void help() {
        if (isObserving || game.getGameOver()) {
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
                    - Resign: "resign"        | Resign from the game.
                    - Exit: "e" "exit"        | Exit the game.
                    - Help: "h" "help"        | List available commands.
                """);
        }
    }

    @Override
    public ClientResult eval(String str, String authToken, int gameId) {
        showHelp = false;
        switch (str) {
            case "m":
            case "move":
                if (!isObserving && !game.getGameOver()) {
                    return move(authToken, gameId);
                }
                else {
                    redraw();
                    PrintUtilities.printError("Error: Unkown Command: '" + str + "'.");
                    System.out.println("Type \"help\" to see available commands.\n");
                    break;
                }

            case "r":
            case "redraw":
                return redraw();

            case "s":
            case "show":
                return showMoves();

            case "resign":
                return resign(authToken, gameId);

            case "e":
            case "exit":
                return exit(authToken, gameId);

            case "h":
            case "help":
                showHelp = true;
                redraw();
                break;

            default:
                redraw();
                PrintUtilities.printError("Error: Unkown Command: '" + str + "'.");
                System.out.println("Type \"help\" to see available commands.\n");
                break;
        }

        return null;
    }

    private ClientResult move(String authToken, int gameId) {
        System.out.print("Start ");
        ChessPosition startPosition = promptForPosition();
        if (startPosition == null) {
            return null;
        }

        System.out.print("End ");
        ChessPosition endPosition = promptForPosition();
        if (endPosition == null) {
            return null;
        }


       
        PieceType promotionType = null;

        ChessPiece piece = game.getBoard().getPiece(startPosition);
        if (clientColor == TeamColor.WHITE && endPosition.getRow() == 8 
            && piece != null && piece.getPieceType() == PieceType.PAWN
        ) {
            System.out.print("Promotion type (");
            for (int i = 0; i < ChessPiece.Promotions.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(ChessPiece.Promotions[i].toString().toLowerCase());
            }
            System.out.print("): ");


            String promotionString = scanner.nextLine();
            switch (promotionString.toLowerCase()) {
                case "queen":
                    promotionType = PieceType.QUEEN;
                    break;
                case "bishop":
                    promotionType = PieceType.BISHOP;
                    break;
                case "knight":
                    promotionType = PieceType.KNIGHT;
                    break;
                case "rook":
                    promotionType = PieceType.ROOK;
                    break;
                default:
                    break;
            }
        }

        ChessMove move = new ChessMove(startPosition, endPosition, promotionType);

        try {
            webSocketFacade.performCommand(new MakeMoveCommand(CommandType.MAKE_MOVE, authToken, gameId, move));
        } 
        catch (Exception e) {
            PrintUtilities.printError(e.getMessage() + ".");
        }

        return null;
    }

    private ClientResult redraw() {
        printScreen(null);
        return null;
    }

    private ClientResult showMoves() {
        ChessPosition position = promptForPosition();
        if (position != null) {
            if (game.getBoard().getPiece(position) != null) {
                printScreen(position);
            }
            else {
                PrintUtilities.printError("Error: no piece at designated position.");
            }
        }
        return null;
    }

    private ClientResult resign(String authToken, int gameId) {

        System.out.print("Are you sure you want to resign? Type (yes) to confirm: ");
        String confirmation = scanner.nextLine();
        if (confirmation != null && !confirmation.toLowerCase().equals("yes")) {
            redraw();
            return null;
        }

        try {
            UserGameCommand gameCommand = new UserGameCommand(CommandType.RESIGN, authToken, gameId);
            this.webSocketFacade.performCommand(gameCommand);
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
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

    private ChessPosition promptForPosition() {
        String letters = "abcdefgh";
        String numbers = "12345678";

        System.out.print("Position: ");
        String position = scanner.nextLine();
        if (position.length() != 2 || letters.indexOf(position.charAt(0)) == -1 || numbers.indexOf(position.charAt(1)) == -1) {
            redraw();
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

    private void printScreen(ChessPosition selectedPosition) {
        PrintUtilities.clearScreen();

        PrintUtilities.printChessBoard(clientColor, game, selectedPosition);

        System.out.println();

        if (game.getGameOver()) {
            if (game.isInStalemate(game.getTeamTurn())) {
                System.out.println("THE GAME ENDED IN STALEMATE");
            }
            else if (game.isInCheckmate(game.getTeamTurn())) {
                TeamColor winningTeam = game.getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
                if (isObserving) {
                    System.out.println("CHECKMATE: %S WON.".formatted(winningTeam));
                }
                else if (winningTeam == clientColor) {
                    PrintUtilities.printSuccess("CONGRATULATIONS! YOU WON!");
                }
                else {
                    PrintUtilities.printError("CHECKMATE: YOU LOST.");
                }
            }
            else {
                PrintUtilities.printMessage(message == null ? "" : message);
                System.out.println("Game Over.");
            }
            System.out.println("\n\nType 'e' or 'exit' to exit the game.");
        }
        else if (message != null && message.contains("resigned")) {
            PrintUtilities.printMessage(message);

            System.out.println();

            System.out.println("Game Over.\n\nType 'e' or 'exit' to exit the game.");

            isObserving = true;
        }
        else {
            PrintUtilities.printMessage(message == null ? "" : message);
            System.out.println();
            System.out.println(game.getTeamTurn() + " to move.");

            if (game.isInCheck(clientColor)) {
                PrintUtilities.printError("YOU ARE IN CHECK");
            }
        }

        System.out.println();

        if (showHelp) {
            help();
        }

        System.out.flush();
    }
}
