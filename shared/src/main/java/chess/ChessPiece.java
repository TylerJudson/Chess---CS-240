package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import chess.ChessGame.TeamColor;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * The possible promotions of a pawn
     */
    public static final PieceType[] Promotions = {
            PieceType.QUEEN,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
    };

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {

        switch (this.getPieceType()) {
            case PieceType.PAWN:
                return pawnMoves(board, position);
            case PieceType.ROOK: 
                return rookMoves(board, position);
        
            default:
                return pawnMoves(board, position);
        }

    }


    /**
     * Calculates all the positions a PAWN can move to
     * 
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        int direction = this.getTeamColor() == TeamColor.WHITE ? 1 : -1;
        int forwardOne = position.getRow() + direction;
        int gameStartRow = this.getTeamColor() == TeamColor.WHITE ? 2 : 7;

        if (1 <= forwardOne && forwardOne <= 8) {

            // Get possible promotions
            PieceType[] possiblePromotions = (forwardOne == 8 || forwardOne == 1) ? Promotions : new PieceType[]{ null };
            
            for (PieceType promotion : possiblePromotions) {

                // Moving forward once 
                if (board.getPiece(new ChessPosition(forwardOne, position.getColumn())) == null) {
                    possibleMoves.add(new ChessMove(position, new ChessPosition(forwardOne, position.getColumn()), promotion));
                }

                // Check diagonals
                if (position.getColumn() + 1 <= 8 
                    && board.getPiece(new ChessPosition(forwardOne, position.getColumn() + 1)) != null
                    && board.getPiece(new ChessPosition(forwardOne, position.getColumn() + 1)).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(position, new ChessPosition(forwardOne, position.getColumn() + 1), promotion));
                }
                else if (position.getColumn() - 1 >= 1 
                    && board.getPiece(new ChessPosition(forwardOne, position.getColumn() - 1)) != null
                    && board.getPiece(new ChessPosition(forwardOne, position.getColumn() - 1)).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(position, new ChessPosition(forwardOne, position.getColumn() - 1), promotion));
                }
            }
        }
        if (position.getRow() == gameStartRow 
        && board.getPiece(new ChessPosition(forwardOne, position.getColumn())) == null 
        && board.getPiece(new ChessPosition(forwardOne + direction, position.getColumn())) == null) {
            possibleMoves.add(new ChessMove(position, new ChessPosition(forwardOne + direction, position.getColumn()), null));
        }

        return possibleMoves;
    }

    /**
     * Calculates all the positions a ROOK can move to
     * 
     * @return Collection of valid moves
     */
    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        // Add towards the up
        for (int i = position.getRow() + 1; i <= 8; i++) {
            ChessPosition newPos = new ChessPosition(i, position.getColumn());
            if (board.getPiece(newPos) != null) {
                if (board.getPiece(newPos).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(position, newPos, null));
                }
                break;
            }
            else {
                possibleMoves.add(new ChessMove(position, newPos, null));
            }
        }

         // Add towards the down
        for (int i = position.getRow() - 1; i >= 1; i--) {
            ChessPosition newPos = new ChessPosition(i, position.getColumn());
            if (board.getPiece(newPos) != null) {
                if (board.getPiece(newPos).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(position, newPos, null));
                }
                break;
            }
            else {
                possibleMoves.add(new ChessMove(position, newPos, null));
            }
        }

        // Add towards the right
        for (int i = position.getColumn() + 1; i <= 8; i++) {
            ChessPosition newPos = new ChessPosition(position.getRow(), i);
            if (board.getPiece(newPos) != null) {
                if (board.getPiece(newPos).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(position, newPos, null));
                }
                break;
            }
            else {
                possibleMoves.add(new ChessMove(position, newPos, null));
            }
        }

        // Add towards the left
        for (int i = position.getColumn() - 1; i >= 1; i--) {
            ChessPosition newPos = new ChessPosition(position.getRow(), i);
            if (board.getPiece(newPos) != null) {
                if (board.getPiece(newPos).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(position, newPos, null));
                }
                break;
            }
            else {
                possibleMoves.add(new ChessMove(position, newPos, null));
            }
        }

        return possibleMoves;
    }





    @Override
    public boolean equals(Object o) {
        if (o instanceof ChessPiece){
            ChessPiece other = (ChessPiece) o;
            
            if (this.pieceColor == other.pieceColor && this.type == other.type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
