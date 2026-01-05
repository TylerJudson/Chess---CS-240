package client;

public class PostloginClient implements Client {

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
    public ClientResult eval(String str) {
        switch (str) {
            case "c":
            case "create":
                return create();
            
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



    private ClientResult create() {
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
