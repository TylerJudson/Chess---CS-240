package client;

import static ui.EscapeSequences.*;

public class PrintUtilities {
    
    static void printSection(String sectionTitle) {
        System.out.println(SET_TEXT_BOLD + SET_TEXT_COLOR_BLUE + "\n" 
                    + sectionTitle + "\n"
                    + "--------------------------------------------\n"
                    + RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR
        );
    }

    static void printSuccess(String message) {
        System.out.println(SET_TEXT_COLOR_GREEN + message + "\n\n" + RESET_TEXT_COLOR);
    }

    static void printError(String message) {
        System.out.println(SET_TEXT_COLOR_RED + message + "\n\n" + RESET_TEXT_COLOR);
    }
}
