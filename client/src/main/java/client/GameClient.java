package client;

import java.util.ArrayList;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import model.GameData;
import server.ServerFacade;

public class GameClient implements Client {

    Scanner scanner = new Scanner(System.in);
    ChessGame game = new ChessGame();
    ServerFacade serverFacade;

    TeamColor clientColor = TeamColor.WHITE;
    boolean isObserving = false;

    public GameClient(ServerFacade serverFacade, String authToken, int gameId, String username) {
        this.serverFacade = serverFacade;
        System.out.println("GAMEDID: " + gameId);
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

        printGameBoard(null);
    }

    @Override
    public void help() {
        System.out.println("""
                Available Commands:
                - Exit: "e" "exit"            | Exit the game.
                - Quit: "q" "quit"            | Quit the application.
                - Help: "h" "help"            | List available commands.
            """);
    }

    @Override
    public ClientResult eval(String str, String authToken, int gameId) {
        switch (str) {
            case "e":
            case "exit":
                return exit();

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

    private ClientResult exit() {
        PrintUtilities.printSuccess("Success: you have exited the game.");
        return new ClientResult(ClientType.POSTLOGIN, null, -1, null);
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
