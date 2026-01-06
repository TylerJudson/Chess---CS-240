package client;

import java.util.Scanner;

import requests.CreateGameRequest;
import results.CreateGameResult;
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
                return list();

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
            CreateGameResult result = this.serverFacade.createGame(new CreateGameRequest(gameName), authToken);

            PrintUtilities.printSuccess("SUCCESS: " + gameName + " was created.");
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return null;
    }

    private ClientResult list() {
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
