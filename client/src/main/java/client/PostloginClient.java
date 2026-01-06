package client;

import static ui.EscapeSequences.*;

import java.util.Scanner;

import model.GameData;
import requests.CreateGameRequest;
import results.ListGamesResult;
import server.ServerFacade;

public class PostloginClient implements Client {
    Scanner scanner = new Scanner(System.in);
    ServerFacade serverFacade;

    public PostloginClient(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    @Override
    public void help() {
        System.out.println("""
                Available Commands:
                - Create Game: "c" "create"   | Creates a new game.
                - List Games: "l" "list"      | List all available games.
                - Join Game: "j" "join"       | Join a game.
                - Observe game: "o" "observe" | Observe a game.
                - Logout: "logout"            | Logout of your account.
                - Quit: "q" "quit"            | Quit the application.
                - Help: "h" "help"            | List available commands.
            """);
    }

    @Override
    public ClientResult eval(String str, String authToken, int GameID) {
        switch (str) {
            case "c":
            case "create":
                return create(authToken);
            
            case "l":
            case "list":
                return list(authToken);

            case "j":
            case "join":
                return join();

            case "o":
            case "observe":
                return observe();

            case "logout":
                return logout();
            
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


    private ClientResult create(String authToken) {
        PrintUtilities.printSection("CREATE GAME");

        System.out.print("Game Name: ");
        String gameName = scanner.nextLine();
        if (gameName.isBlank()) {
            PrintUtilities.printError("Error: Game Name can't be blank.");
            return null;
        }

        try {
            this.serverFacade.createGame(new CreateGameRequest(gameName), authToken);

            PrintUtilities.printSuccess("SUCCESS: " + gameName + " was created.");
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return null;
    }

    private ClientResult list(String authToken) {
        PrintUtilities.printSection("AVAILABLE GAMES");
        try {
            ListGamesResult result = serverFacade.listGames(authToken);
            for (int i = 0; i < result.games().size(); i++) {
                GameData game = result.games().get(i);
                System.out.print("%d. %s".formatted(i, game.gameName()));
                if (game.whiteUsername() != null) {
                    System.out.print(SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + game.whiteUsername());
                }
                if (game.blackUsername() != null) {
                     System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + game.blackUsername());
                }
                System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
            }
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }
        return null;
    }

    private ClientResult join() {
        return null;
    }

    private ClientResult observe() {
        return null;
    }

    private ClientResult logout() {
        return null;
    }
    
}
