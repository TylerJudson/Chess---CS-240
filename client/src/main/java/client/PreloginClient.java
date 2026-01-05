package client;
import static ui.EscapeSequences.*;

public class PreloginClient implements Client {

    @Override
    public void help() {
        System.out.println("""
                Available Commands:
                - Register: "r" "register" | Create a new account.
                - Login: "l" "login"       | Login into your account.
                - Quit: "q" "quit"         | Quit the application.
                - Help: "h" "help"         | List available commands.
            """);
    }

    @Override
    public ClientType eval(String str) {
        switch (str) {
            case "r":
            case "register":
                register();
                break;
            
            case "l":
            case "login":
                login();
                break;
            
            case "h":
            case "help":
                help();
                break;

            default:
                System.out.println(SET_TEXT_COLOR_RED + "Error: Unkown Command: " + str + "."
                                    + RESET_TEXT_COLOR + " Type \"help\" to see available commands.\n");
                break;
        }

        return null;
    }


    private void register() {

    }
    
    private void login() {

    }    
}
