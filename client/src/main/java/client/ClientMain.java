package client;

import java.util.Scanner;

import server.ServerFacade;

import static ui.EscapeSequences.*;

public class ClientMain {

    static Client client;
    static ServerFacade serverFacade;

    public static void main(String[] args) {
        serverFacade = new ServerFacade("http://localhost:8080");
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
                ClientResult newClient = client.eval(line.toLowerCase());
                if (newClient != null) {
                    if (newClient.newClient() == ClientType.PRELOGIN) {
    
                    }
                    else if (newClient.newClient() == ClientType.POSTLOGIN) {
    
                    }
                    else if (newClient.newClient() == ClientType.GAME) {
    
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
