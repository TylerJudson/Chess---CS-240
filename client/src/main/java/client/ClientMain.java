package client;

import java.util.Scanner;

import server.ServerFacade;

import static ui.EscapeSequences.*;

public class ClientMain {

    private static Client client;
    private static ServerFacade serverFacade;
    private static String currentAuth;
    private static int currentGameID;
    private static String currentUsername;
    private static String serverUrl = "http://localhost:8080";

    public static void main(String[] args) {
        serverFacade = new ServerFacade(serverUrl);
        client = new PreloginClient(serverFacade);
        run();
    }


    public static void run() {
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE +
                    """


                    --------------------------------------------
                    |            ♕ Chess Client ♕              |
                    --------------------------------------------
                    """ +
                    RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR
        );


        client.help();

        Scanner scanner = new Scanner(System.in);
        System.out.print(">> ");
        String line = scanner.nextLine();
        while (!line.equals("quit") && !line.equals("q")) {
            try {
                ClientResult clientResult = client.eval(line.toLowerCase(), currentAuth, currentGameID);
                if (clientResult != null) {
                    currentAuth = clientResult.authToken() == null ? currentAuth : clientResult.authToken();
                    currentGameID = clientResult.gameID() < 1 ? currentGameID : clientResult.gameID();
                    currentUsername = clientResult.username() == null ? currentUsername : clientResult.username();

                    if (clientResult.newClient() == ClientType.PRELOGIN) {
                        currentAuth = null;
                        currentGameID = -1;
                        currentUsername = null;
                        client = new PreloginClient(serverFacade);
                        client.help();
                    }
                    else if (clientResult.newClient() == ClientType.POSTLOGIN) {
                        currentGameID = -1;
                        client = new PostloginClient(serverFacade);
                        client.help();
                    }
                    else if (clientResult.newClient() == ClientType.GAME) {
                        client = new GameClient(serverFacade, serverUrl, currentAuth, currentGameID, currentUsername);
                    }
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }

            System.out.print(">> ");
            line = scanner.nextLine();
        }

        scanner.close();
        System.out.println();
    }
}
