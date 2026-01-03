package chess;

import java.util.Arrays;
import java.util.Objects;

import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares.length; j++) {
                squares[i][j] = null;
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Moves a given piece
     * The piece will replace any piece including its own color
     * 
     * @param move The chess move to perform
     */
    public void movePiece(ChessMove move) {
        ChessPiece pieceToMove = this.getPiece(move.getStartPosition());
        
        this.addPiece(move.getEndPosition(), pieceToMove);
        this.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            pieceToMove.promotePawn(move.getPromotionPiece());
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Place pawns for both sides
        for (int i = 0; i < squares.length; i++) {
            addPiece(new ChessPosition(2, i + 1), new ChessPiece(TeamColor.WHITE, PieceType.PAWN));
            addPiece(new ChessPosition(7, i + 1), new ChessPiece(TeamColor.BLACK, PieceType.PAWN));
        }

        // Place rooks
        addPiece(new ChessPosition(1, 1), new ChessPiece(TeamColor.WHITE, PieceType.ROOK));
        addPiece(new ChessPosition(1, 8), new ChessPiece(TeamColor.WHITE, PieceType.ROOK));
        addPiece(new ChessPosition(8, 1), new ChessPiece(TeamColor.BLACK, PieceType.ROOK));
        addPiece(new ChessPosition(8, 8), new ChessPiece(TeamColor.BLACK, PieceType.ROOK));
        
        // Place knights
        addPiece(new ChessPosition(1, 2), new ChessPiece(TeamColor.WHITE, PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 7), new ChessPiece(TeamColor.WHITE, PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 2), new ChessPiece(TeamColor.BLACK, PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 7), new ChessPiece(TeamColor.BLACK, PieceType.KNIGHT));

        // Place bishops
        addPiece(new ChessPosition(1, 3), new ChessPiece(TeamColor.WHITE, PieceType.BISHOP));
        addPiece(new ChessPosition(1, 6), new ChessPiece(TeamColor.WHITE, PieceType.BISHOP));
        addPiece(new ChessPosition(8, 3), new ChessPiece(TeamColor.BLACK, PieceType.BISHOP));
        addPiece(new ChessPosition(8, 6), new ChessPiece(TeamColor.BLACK, PieceType.BISHOP));

        // Place queens
        addPiece(new ChessPosition(1, 4), new ChessPiece(TeamColor.WHITE, PieceType.QUEEN));
        addPiece(new ChessPosition(8, 4), new ChessPiece(TeamColor.BLACK, PieceType.QUEEN));

        // Place kings
        addPiece(new ChessPosition(1, 5), new ChessPiece(TeamColor.WHITE, PieceType.KING));
        addPiece(new ChessPosition(8, 5), new ChessPiece(TeamColor.BLACK, PieceType.KING));
    }


    /**
     * Performs a deep copy on a given chessboard
     * 
     * @param original the original chessboard to copy
     * @return the copy of the chessboard
     */
    static public ChessBoard deepCopy(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                copy.addPiece(new ChessPosition(i, j), original.getPiece(new ChessPosition(i, j)));
            }
        }
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessBoard)) { 
            return false; 
        }

        ChessBoard other = (ChessBoard) o;
        
        for (int i = 0; i < squares.length; i++) {
            for (int j = 0; j < squares.length; j++) {
                ChessPosition pos = new ChessPosition(i + 1, j + 1);
                if (!Objects.equals(this.getPiece(pos), other.getPiece(pos))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
