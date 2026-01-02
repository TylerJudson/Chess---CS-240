package chess;

import java.util.Objects;

import chess.ChessPiece.PieceType;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.promotionPiece;
    }



    @Override
    public boolean equals(Object o) {
        if (o instanceof ChessMove) {    
            ChessMove other = (ChessMove) o;

            if (Objects.equals(this.getStartPosition(), other.getStartPosition()) 
                && Objects.equals(this.getEndPosition(), other.getEndPosition())
                && Objects.equals(this.getPromotionPiece(), other.getPromotionPiece())) {
                    return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.startPosition, this.endPosition, this.promotionPiece);
    }

    @Override
    public String toString() {
        return "Start: [%d, %d]; End: [%d, %d]; PP: %s".formatted(this.startPosition.getRow(), this.startPosition.getColumn(), this.endPosition.getRow(), this.endPosition.getColumn(), this.promotionPiece);
    }

}
