package chess;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    ChessBoard board;
    TeamColor currentTeam;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTeam = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece requestedPiece = this.board.getPiece(startPosition);
        if (requestedPiece == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = new ArrayList<ChessMove>();
        for (ChessMove possibleMove : requestedPiece.pieceMoves(this.board, startPosition)) {
            ChessBoard copiedBoard = ChessBoard.deepCopy(this.getBoard());
            copiedBoard.movePiece(possibleMove);
            if (!this.isInCheckGivenBoard(copiedBoard, requestedPiece.getTeamColor())) {
                possibleMoves.add(possibleMove);
            }
        }

        return possibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for 
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckGivenBoard(this.getBoard(), teamColor);
    }

    /**
     * Determins if the given team is in check on a particular board
     * 
     * @param board the board to check on 
     * @param teamColor which team to check for
     * @return True if the specified team is in check on the given board
     */
    public boolean isInCheckGivenBoard(ChessBoard board, TeamColor teamColor) {
         // Find the position of the king
        ChessPosition kingPosition = this.findKing(board, teamColor);

        // Loop through all of the oppenents pieces
        for (ChessPosition opponentPosition : this.findAllPositionsOfPieces(board, teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE)) {
            ChessPiece opponentPiece = board.getPiece(opponentPosition);
            
            // Loop through all of the moves to determine if they match the king's position
            for (ChessMove move : opponentPiece.pieceMoves(board, opponentPosition)) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * Finds the position of the king for the given team color
     * 
     * @param board the chessboard to check on
     * @param teamColor the team color to find the king for
     * @return the king or null if it doesn't exist
     */
    public ChessPosition findKing(ChessBoard board, TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == PieceType.KING) {
                    return new ChessPosition(i, j);
                }
            }
        }
        return null;
    }

    /**
     * Finds all of the pieces on the board for a given color
     * 
     * @param board the chessboard to check
     * @param teamColor the particular color
     * @return a list of all of the pieces
     */
    public Collection<ChessPiece> findAllPieces(ChessBoard board, TeamColor teamColor) {
        Collection<ChessPiece> pieces = new ArrayList<ChessPiece>();

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() == teamColor) {
                    pieces.add(piece);
                }
            }
        }

        return pieces;
    }

    /**
     * Finds all of the positions of a given TeamColor's pieces
     * 
     * @param board the chessboard to check
     * @param teamColor the given TeamColor
     * @return the list of positions
     */
    public Collection<ChessPosition> findAllPositionsOfPieces(ChessBoard board, TeamColor teamColor) {
        Collection<ChessPosition> positions = new ArrayList<ChessPosition>();

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() == teamColor) {
                    positions.add(new ChessPosition(i, j));
                }
            }
        }

        return positions;
    }



}
