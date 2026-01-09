package client;

import java.util.Scanner;

import requests.LoginRequest;
import requests.RegisterRequest;
import results.LoginResult;
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
    public ClientResult eval(String str, String authToken, int gameID) {
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
                PrintUtilities.printError("Error: Unkown Command: '" + str + "'.");
                System.out.println("Type \"help\" to see available commands.\n");
                break;
        }

        return null;
    }

    private ClientResult register() {
        PrintUtilities.printSection("REGISTER");

        PromptResult promptResult = promptUsernameAndPassword();
        if (promptResult == null) {
            return null;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine();
        if (email.isBlank()) {
            PrintUtilities.printError("Error: email can't be blank.");
            return null;
        }

        try {
            RegisterResult result = this.serverFacade.register(new RegisterRequest(promptResult.username(), 
                                            promptResult.password(), email));

            PrintUtilities.printSuccess("Success: your account was created with username '" + result.username() + "'.");

            return new ClientResult(ClientType.POSTLOGIN, result.authToken(), -1, promptResult.username());
        }
        catch (Exception ex) {
            PrintUtilities.printError(ex.getMessage() + ".");
        }

        return null;
    }
    
    private ClientResult login() {
        PrintUtilities.printSection("LOGIN");
        
        PromptResult promptResult = promptUsernameAndPassword();
        if (promptResult == null) {
            return null;
        }

        try {
            LoginResult result = this.serverFacade.login(new LoginRequest(promptResult.username(), promptResult.password()));

            PrintUtilities.printSuccess("Success: your are now logged in.");

            return new ClientResult(ClientType.POSTLOGIN, result.authToken(), -1, promptResult.username());
        }
        catch (Exception ex) {
            if (ex.getMessage().equals("Error: unauthorized")) {
                PrintUtilities.printError("Error: username or password is invalid.");
            }
            else {
                PrintUtilities.printError(ex.getMessage() + ".");
            }
        }

        return null;
    }    


    private PromptResult promptUsernameAndPassword() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        if (username.isBlank()) {
            PrintUtilities.printError("Error: username can't be blank.");
            return null;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.isBlank()) {
            PrintUtilities.printError("Error: password can't be blank.");
            return null;
        }
        
        return new PromptResult(username, password);
    }
}

record PromptResult(String username, String password) {}
