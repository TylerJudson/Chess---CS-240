package client;
import static ui.EscapeSequences.*;

import java.util.Scanner;

import exceptions.ResponseException;
import requests.RegisterRequest;
import results.RegisterResult;
import server.ServerFacade;

public class PreloginClient implements Client {
    Scanner scanner = new Scanner(System.in);
    ServerFacade serverFacade;

    public PreloginClient(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

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

        PromptResult promptResult = promptUsernameAndPassword();
        if (promptResult == null) {
            return null;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine();
        if (email.isBlank()) {
            System.out.println(SET_TEXT_COLOR_RED + "Error: email can't be blank.\n\n" + RESET_TEXT_COLOR);
            return null;
        }

        try {
            RegisterResult result = this.serverFacade.register(new RegisterRequest(promptResult.username(), 
                                            promptResult.password(), email));

            System.out.println(SET_TEXT_COLOR_GREEN + "SUCCESS: your account was created with username '" 
                                + result.username() + "'.\n\n" + RESET_TEXT_COLOR);

            return new ClientResult(ClientType.POSTLOGIN, result.authToken());
        }
        catch (Exception ex) {
            System.out.println(SET_TEXT_COLOR_RED + ex.getMessage() + ".\n\n" + RESET_TEXT_COLOR);
        }

        return null;
    }
    
    private ClientResult login() {
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_LIGHT_GREY +
                    """

                    LOGIN
                    --------------------------------------------
                    """ +
                    RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR
        );
        PromptResult promptResult = promptUsernameAndPassword();
        if (promptResult == null) {
            return null;
        }

        return null;
    }    


    private PromptResult promptUsernameAndPassword() {
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
        
        return new PromptResult(username, password);
    }
}

record PromptResult(String username, String password) {}
