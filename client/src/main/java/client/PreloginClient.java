package client;
import static ui.EscapeSequences.*;

import java.util.Scanner;

public class PreloginClient implements Client {
    Scanner scanner = new Scanner(System.in);

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
    public ClientResult eval(String str) {
        switch (str) {
            case "r":
            case "register":
                return register();
            
            case "l":
            case "login":
                return login();
            
            case "h":
            case "help":
                help();
                break;

            default:
                System.out.println(SET_TEXT_COLOR_RED + "Error: Unkown Command: '" + str + "'."
                                    + RESET_TEXT_COLOR + " Type \"help\" to see available commands.\n");
                break;
        }

        return null;
    }


    private ClientResult register() {
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_DARK_GREY +
                    """

                    REGISTER
                    --------------------------------------------
                    """ +
                    RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR
        );

        return promptUsernameAndPassword();
    }
    
    private ClientResult login() {
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY +
                    """

                    LOGIN
                    --------------------------------------------
                    """ +
                    RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR
        );
        return promptUsernameAndPassword();
    }    


    private ClientResult promptUsernameAndPassword() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        if (username.isBlank()) {
            System.out.println(SET_TEXT_COLOR_RED + "Error: username can't be blank.\n\n" + RESET_TEXT_COLOR);
            return null;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.isBlank()) {
            System.out.println(SET_TEXT_COLOR_RED + "Error: password can't be blank.\n\n" + RESET_TEXT_COLOR);
            return null;
        }
        
        return null;
    }
}
