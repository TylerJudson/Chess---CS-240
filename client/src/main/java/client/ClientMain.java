package client;

import java.util.Scanner;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessGame.TeamColor;
import server.ServerFacade;

import static ui.EscapeSequences.*;

public class ClientMain {

    private static Client client;
    private static ServerFacade serverFacade;
    private static String currentAuth;
    private static int currentGameID;

    public static void main(String[] args) {
        serverFacade = new ServerFacade("http://localhost:8080");
        client = new PreloginClient(serverFacade);
        PrintUtilities.printChessBoard(TeamColor.BLACK, new ChessGame().getBoard());
        // run();
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
                    currentGameID = clientResult.GameID() > 0 ? currentGameID : clientResult.GameID();

                    if (clientResult.newClient() == ClientType.PRELOGIN) {
                        client = new PreloginClient(serverFacade);
                        client.help();
                    }
                    else if (clientResult.newClient() == ClientType.POSTLOGIN) {
                        client = new PostloginClient(serverFacade);
                        client.help();
                    }
                    else if (clientResult.newClient() == ClientType.GAME) {
                        
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
