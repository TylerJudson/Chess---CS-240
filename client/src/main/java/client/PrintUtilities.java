package client;

import static ui.EscapeSequences.*;

import chess.ChessBoard;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPiece.PieceType;

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




    static void printChessBoard(TeamColor color, ChessBoard board) {
        char[] letters = (color == TeamColor.WHITE ? "abcdefgh" : "hgfedcba").toCharArray();
        char[] numbers = (color == TeamColor.WHITE ? "87654321" : "12345678").toCharArray();

        System.out.println(SET_TEXT_BOLD);

        // draw corner
        drawSquare();

        // draw letters
        for (char letter : letters) {
            drawSquare(letter);
        }

        // draw corner
        drawSquare();
        System.out.println();


        for (int i = 0; i < 8; i++) {
            drawSquare(numbers[i]);
            for (int j = 0; j < 8; j++) {
                int row = color == TeamColor.WHITE ? 9 - (i + 1) : i + 1;
                int col = color == TeamColor.WHITE ? j + 1 : 9 - (j + 1);
                ChessPosition position = new ChessPosition(row, col);
                drawSquare((j + i) % 2 == 0 ? TeamColor.WHITE : TeamColor.BLACK, color, board.getPiece(position));
            }
            drawSquare(numbers[i]);
            System.out.println();
        }

        // draw corner
        drawSquare();

        // draw letters
        for (char letter : letters) {
            drawSquare(letter);
        }

        // draw corner
        drawSquare();
        System.out.println();
    }



    static void drawSquare(TeamColor color) {
        if (color == TeamColor.WHITE) {
            System.out.print(SET_BG_COLOR_WHITE + EMPTY + RESET_BG_COLOR);
        }
        else {
            System.out.print(SET_BG_COLOR_BLACK + EMPTY + RESET_BG_COLOR);
        }
    }

    static void drawSquare() {
        System.out.print(SET_BG_COLOR_LIGHT_GREY + EMPTY + RESET_BG_COLOR);
    }

    static void drawSquare(char value) {
        System.out.print(SET_BG_COLOR_LIGHT_GREY + "\u2003" + value + " " + RESET_BG_COLOR);
    }

    static void drawSquare(TeamColor squareColor, TeamColor teamColor, ChessPiece piece) {
        String textColor = "";
        String bgColor = squareColor == TeamColor.WHITE ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;
        String pieceString = EMPTY;
        if (piece != null) {
            textColor = teamColor == piece.getTeamColor() ? SET_TEXT_COLOR_BLUE : SET_TEXT_COLOR_RED;
            switch (piece.getPieceType()) {
                case PieceType.PAWN:
                    pieceString = WHITE_PAWN;
                    break;
                case PieceType.ROOK: 
                    pieceString = WHITE_ROOK;
                    break;
                case PieceType.KNIGHT:
                    pieceString = WHITE_KNIGHT;
                    break;
                case PieceType.BISHOP:
                    pieceString = WHITE_BISHOP;
                    break;
                case PieceType.QUEEN:
                    pieceString = WHITE_QUEEN;
                    break;
                case PieceType.KING:
                    pieceString = WHITE_KING;
                    break;
                default:
                    break;
            }
        }
        System.out.print(bgColor + textColor + pieceString + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }
}
