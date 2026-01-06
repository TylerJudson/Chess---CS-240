package client;

import java.util.ArrayList;
import java.util.Scanner;

import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
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
                return join(authToken);

            case "o":
            case "observe":
                return observe(authToken);

            case "logout":
                return logout(authToken);
            
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
            PrintUtilities.printError("Error: game name can't be blank.");
            return null;
        }

        try {
            this.serverFacade.createGame(new CreateGameRequest(gameName), authToken);

            PrintUtilities.printSuccess("Success: " + gameName + " was created.");
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
            if (result.games().size() == 0) {
                System.out.println("There are no available games.\n\n");
                return null;
            }
            for (int i = 0; i < result.games().size(); i++) {
                GameData game = result.games().get(i);
                System.out.println("%d. %s".formatted(i + 1, game.gameName()));
                if (game.whiteUsername() != null) {
                    System.out.println("  └ White: " + game.whiteUsername());
                }
                if (game.blackUsername() != null) {
                     System.out.println("  └ Black: " + game.blackUsername());
                }
            }
            System.out.println("\n\n");
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }
        return null;
    }

    private ClientResult join(String authToken) {
        PrintUtilities.printSection("JOIN GAME");

        System.out.print("Game Id: ");
        String gameId = scanner.nextLine();
        if (gameId.isBlank()) {
            PrintUtilities.printError("Error: game id can't be blank.");
            return null;
        }
        System.out.print("Team Color (w|b): ");
        String teamColor = scanner.nextLine().trim().toLowerCase();
        if (!teamColor.equals("w") && !teamColor.equals("b")) {
            PrintUtilities.printError("Error: team color must be w or b.");
            return null;
        }

        try {
            int intGameId = Integer.parseInt(gameId);
            ArrayList<GameData> games = serverFacade.listGames(authToken).games();
            if (1 > intGameId || intGameId > games.size() + 1) {
                PrintUtilities.printError("Error: invalid game id.");
                return null;
            }

            String joinColor = teamColor.equals("w") ? "WHITE" : "BLACK";
            GameData game = games.get(intGameId - 1);

            serverFacade.joinGame(new JoinGameRequest(joinColor, game.gameID()), authToken);
            
            PrintUtilities.printSuccess("Success: you have joined the game '%s'".formatted(game.gameName()));

            return new ClientResult(ClientType.GAME, authToken, game.gameID(), null);
        }
        catch (NumberFormatException ex) {
            PrintUtilities.printError("Error: invalid game id.");
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return null;
    }

    private ClientResult observe(String authToken) {
        PrintUtilities.printSection("OBSERVE GAME");

        System.out.print("Game Id: ");
        String gameId = scanner.nextLine();
        if (gameId.isBlank()) {
            PrintUtilities.printError("Error: game id can't be blank.");
            return null;
        }

        try {
            int intGameId = Integer.parseInt(gameId);
            ArrayList<GameData> games = serverFacade.listGames(authToken).games();
            if (1 > intGameId || intGameId > games.size() + 1) {
                PrintUtilities.printError("Error: invalid game id.");
                return null;
            }

            GameData game = games.get(intGameId - 1);
            
            PrintUtilities.printSuccess("Success: you are now observing the game '%s'".formatted(game.gameName()));

            return new ClientResult(ClientType.GAME, authToken, game.gameID(), null);
        }
        catch (NumberFormatException ex) {
            PrintUtilities.printError("Error: invalid game id.");
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return null;
    }

    private ClientResult logout(String authToken) {
        try {
            serverFacade.logout(authToken);
        }
        catch (Exception ex) {}
        
        PrintUtilities.printSuccess("Success: you have been logged out");
        return new ClientResult(ClientType.PRELOGIN, null, -1, null);
    }
    
}
