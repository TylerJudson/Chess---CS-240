package chess;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static final PieceType[] PROMOTIONS = {
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
    
    public void promotePawn(PieceType promotion) {
        if (Arrays.asList(PROMOTIONS).contains(promotion)) {
            this.type = promotion;
        }
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
            case PieceType.KNIGHT:
                return knightMoves(board, position);
            case PieceType.BISHOP:
                return bishopMoves(board, position);
            case PieceType.QUEEN:
                return queenMoves(board, position);
            case PieceType.KING:
                return kingMoves(board, position);
        
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
            PieceType[] possiblePromotions = (forwardOne == 8 || forwardOne == 1) ? PROMOTIONS : new PieceType[]{ null };
            
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

        possibleMoves.addAll(getMovesInDirection(board, position, 1, 0));
        possibleMoves.addAll(getMovesInDirection(board, position, -1, 0));
        possibleMoves.addAll(getMovesInDirection(board, position, 0, 1));
        possibleMoves.addAll(getMovesInDirection(board, position, 0, -1));

        return possibleMoves;
    }

    /**
     * Calculates all the positions a KNIGHT can move to
     * 
     * @return Collection of valid moves
     */
    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        ChessPosition[] endPositions = {
            // UP - Right and Left
            new ChessPosition(position.getRow() + 2, position.getColumn() + 1),
            new ChessPosition(position.getRow() + 2, position.getColumn() - 1),
            // RIGHT - Up and Down
            new ChessPosition(position.getRow() + 1, position.getColumn() + 2),
            new ChessPosition(position.getRow() - 1, position.getColumn() + 2),
            // DOWN - Right and Left
            new ChessPosition(position.getRow() - 2, position.getColumn() + 1),
            new ChessPosition(position.getRow() - 2, position.getColumn() - 1),
            // LEFT - Up and Down
            new ChessPosition(position.getRow() + 1, position.getColumn() - 2),
            new ChessPosition(position.getRow() - 1, position.getColumn() - 2),
        };

        possibleMoves.addAll(extractChessMovesFromPositions(board, position, endPositions));

        return possibleMoves;
    }

    /**
     * Calculates all the positions a BISHOP can move to
     * 
     * @return Collection of valid moves
     */
    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        possibleMoves.addAll(getMovesInDirection(board, position, 1, 1));
        possibleMoves.addAll(getMovesInDirection(board, position, -1, 1));
        possibleMoves.addAll(getMovesInDirection(board, position, 1, -1));
        possibleMoves.addAll(getMovesInDirection(board, position, -1, -1));

        return possibleMoves;
    }

    
    /**
     * Calculates all the positions a QUEEN can move to
     * 
     * @return Collection of valid moves
     */
    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        possibleMoves.addAll(rookMoves(board, position));
        possibleMoves.addAll(bishopMoves(board, position));

        return possibleMoves;
    }

    /**
     * Calculates all the positions a KING can move to
     * 
     * @return Collection of valid moves
     */
    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        ChessPosition[] endPositions = {
            new ChessPosition(position.getRow() + 1, position.getColumn()),
            new ChessPosition(position.getRow() + 1, position.getColumn() + 1),
            new ChessPosition(position.getRow(), position.getColumn() + 1),
            new ChessPosition(position.getRow() - 1, position.getColumn() + 1),
            new ChessPosition(position.getRow() - 1, position.getColumn()),
            new ChessPosition(position.getRow() - 1, position.getColumn() - 1),
            new ChessPosition(position.getRow(), position.getColumn() - 1),
            new ChessPosition(position.getRow() + 1, position.getColumn() - 1),
        };

        possibleMoves.addAll(extractChessMovesFromPositions(board, position, endPositions));

        // Check castling conditions
        int row = board.getPiece(position).pieceColor == TeamColor.WHITE ? 1 : 8;
        // King must be in position to castle
        if (position.equals(new ChessPosition(row, 5))) {
            // Rook must be in position to king-side castle
            ChessPiece kingSideRook = board.getPiece(new ChessPosition(row, 8));
            if (kingSideRook != null && kingSideRook.getPieceType() == PieceType.ROOK) {
                // There cannot be any pieces in between
                if (board.getPiece(new ChessPosition(row, 6)) == null && board.getPiece(new ChessPosition(row, 7)) == null) {
                    possibleMoves.add(new ChessMove(position, new ChessPosition(row, 7), null));
                }
            }
            // Rook must be in position to queen-side castle
            ChessPiece queenSideRook = board.getPiece(new ChessPosition(row, 1));
            if (queenSideRook != null && queenSideRook.getPieceType() == PieceType.ROOK) {
                // There cannot be any pieces in between
                if (board.getPiece(new ChessPosition(row, 2)) == null 
                    && board.getPiece(new ChessPosition(row, 3)) == null 
                    && board.getPiece(new ChessPosition(row, 4)) == null
                ) {
                    possibleMoves.add(new ChessMove(position, new ChessPosition(row, 3), null));
                }
            }
        }

        return possibleMoves;
    }
    

    private Collection<ChessMove> getMovesInDirection(ChessBoard board, ChessPosition pos, int rowChange, int colChange) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();

        int row = pos.getRow() + rowChange;
        int col = pos.getColumn() + colChange;

        while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
            ChessPosition newPos = new ChessPosition(row, col);
            if (board.getPiece(newPos) != null) {
                if (board.getPiece(newPos).pieceColor != this.pieceColor) {
                    possibleMoves.add(new ChessMove(pos, newPos, null));
                }
                break;
            }
            else {
                possibleMoves.add(new ChessMove(pos, newPos, null));
            }
            row += rowChange;
            col += colChange;
        }

        return possibleMoves;
    }

    private Collection<ChessMove> extractChessMovesFromPositions(ChessBoard board, ChessPosition startPosition, ChessPosition[] positions) {
        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        for (ChessPosition chessPosition : positions) {
            if (1 <= chessPosition.getRow() && chessPosition.getRow() <= 8
            && 1 <= chessPosition.getColumn() && chessPosition.getColumn() <= 8
            ) {
                ChessPiece boardPiece = board.getPiece(chessPosition);
                if (boardPiece == null || boardPiece.pieceColor != this.getTeamColor()) {
                    possibleMoves.add(new ChessMove(startPosition, chessPosition, null));
                }
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
