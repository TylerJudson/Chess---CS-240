package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTeam;

    // These variables determine whether the p
    private boolean whiteKingSideCastlingHasMoved;
    private boolean whiteQueenSideCastlingHasMoved;
    private boolean blackKingSideCastlingHasMoved;
    private boolean blackQueenSideCastlingHasMoved;

    private ChessMove previousMove;

    private boolean gameOver;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTeam = TeamColor.WHITE;

        this.whiteKingSideCastlingHasMoved = false;
        this.whiteQueenSideCastlingHasMoved = false;
        this.blackKingSideCastlingHasMoved = false;
        this.blackQueenSideCastlingHasMoved = false;

        this.previousMove = null;

        this.gameOver = false;
    }

    public TeamColor getTeamTurn() {
        return this.currentTeam;
    }

    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }
    
    public void changeTeamTurn() {
        this.setTeamTurn(this.getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    public ChessMove getPreviousMove() {
        return this.previousMove;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Get's whether the game is over or not.
     * @return
     */
    public boolean getGameOver() {
        return this.gameOver;
    }

    /**
     * Set's the status of the game
     * @param gameOver
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
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

            // If the move is castling and we can't castle don't add the move
            if (this.isMoveCastling(possibleMove) && !this.canCastle(possibleMove)) {
                continue;
            }

            if (!isInCheckAftermove(possibleMove)) {
                possibleMoves.add(possibleMove);
            }
         }

        ChessMove possibleEnPassantMove = getEnPassantMove(startPosition);
        if (possibleEnPassantMove != null) {
            possibleMoves.add(possibleEnPassantMove);
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
        if (!isValidMove(move, this.getTeamTurn()) || getGameOver()) {
            throw new InvalidMoveException();
        }

        this.updateCastlingHasMoved(move);

        if (this.isMoveCastling(move)) {
            this.getBoard().movePiece(move);
            if (move.getEndPosition().getColumn() == 7) {
                this.getBoard().movePiece(
                    new ChessMove(
                        new ChessPosition(move.getStartPosition().getRow(), 8), 
                        new ChessPosition(move.getStartPosition().getRow(), 6), 
                        null)
                );
            }
            else {
                this.getBoard().movePiece(
                    new ChessMove(
                        new ChessPosition(move.getStartPosition().getRow(), 1), 
                        new ChessPosition(move.getStartPosition().getRow(), 4), 
                        null)
                );
            }
        }
        else if (this.isEnPassantMove(move)) {
            this.getBoard().movePiece(move);
            this.getBoard().addPiece(this.previousMove.getEndPosition(), null);
        }
        else {
            this.getBoard().movePiece(move);
        }

        this.previousMove = move;
        this.changeTeamTurn();

        if (isInCheckmate(this.getTeamTurn())) {
            setGameOver(true);
        }
        else if (isInStalemate(this.getTeamTurn())) {
            setGameOver(true);
        }
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

        TeamColor oppositeTeamColor = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;

        // Loop through all of the oppenents pieces
        for (ChessPosition opponentPosition : this.findAllPositionsOfPieces(board, oppositeTeamColor)) {
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
     * Determine if a move will cause the team to be in check
     * @param move the move to check
     * @return whether they will be in check or not
     */
    public boolean isInCheckAftermove(ChessMove move) {
        TeamColor pieceColor= this.getBoard().getPiece(move.getStartPosition()).getTeamColor();
        ChessBoard copiedBoard = ChessBoard.deepCopy(this.getBoard());
        copiedBoard.movePiece(move);
        if (!this.isInCheckGivenBoard(copiedBoard, pieceColor)) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (this.isInCheck(teamColor)) {
            
            // Loop through all of the team's pieces on the board   
            for (ChessPosition piecePosition : this.findAllPositionsOfPieces(this.getBoard(), teamColor)) {
                
                // If any piece has a valid move then the king is not in checkmate
                if (!this.validMoves(piecePosition).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
         if (!this.isInCheck(teamColor)) {
            
            // Loop through all of the team's pieces on the board   
            for (ChessPosition piecePosition : this.findAllPositionsOfPieces(this.getBoard(), teamColor)) {
                
                // If any piece has a valid move then the king is not in checkmate
                if (!this.validMoves(piecePosition).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
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


    /**
     * Determines if a move is valid
     * 
     * @param move the move to check
     * @param teamColor the color of the current team
     * @return whether it is valid or not
     */
    public boolean isValidMove(ChessMove move, TeamColor teamcolor) {
        ChessPiece piece = this.getBoard().getPiece(move.getStartPosition());
        if (piece != null && piece.getTeamColor() == teamcolor && this.validMoves(move.getStartPosition()).contains(move)) {
            return true;
        }
        return false;
    }

    /**
     * Updates whether the pieces have moved that determine if the king can castle
     * 
     * @param move
     */
    private void updateCastlingHasMoved(ChessMove move) {
        ChessPiece piece = this.getBoard().getPiece(move.getStartPosition());
        if (piece.getPieceType() == PieceType.KING) {
            if (this.getTeamTurn() == TeamColor.WHITE) {
                this.whiteKingSideCastlingHasMoved = true;
                this.whiteQueenSideCastlingHasMoved = true;
            }
            else {
                this.blackKingSideCastlingHasMoved = true;
                this.blackQueenSideCastlingHasMoved = true;
            }
        }
        else if (piece.getPieceType() == PieceType.ROOK) {
            if (move.getStartPosition().equals(new ChessPosition(1, 8)) && this.getTeamTurn() == TeamColor.WHITE) {
                this.whiteKingSideCastlingHasMoved = true;
            }
            else if (move.getStartPosition().equals(new ChessPosition(1, 1)) && this.getTeamTurn() == TeamColor.WHITE) {
                this.whiteQueenSideCastlingHasMoved = true;
            }
            else if (move.getStartPosition().equals(new ChessPosition(8, 8)) && this.getTeamTurn() == TeamColor.BLACK) {
                this.blackKingSideCastlingHasMoved = true;
            }
            else if (move.getStartPosition().equals(new ChessPosition(8, 1)) && this.getTeamTurn() == TeamColor.BLACK) {
                this.blackQueenSideCastlingHasMoved = true;
            }
        }
    }


    /**
     * Checks if a move is considered castling
     * 
     * @param move
     * @return
     */
    private boolean isMoveCastling(ChessMove move) {
        int row = board.getPiece(move.getStartPosition()).getTeamColor() == TeamColor.WHITE ? 1 : 8;
        if (this.getBoard().getPiece(move.getStartPosition()).getPieceType() == PieceType.KING
            && move.getStartPosition().equals(new ChessPosition(row, 5))
            && Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2) {
                return true;
            }
        return false;
    }

    /**
     * Determins if we can castle based if a move is considered castling
     * and if the side's pieces have not moved. 
     * 
     * @param move
     * @return
     */
    private boolean canCastle(ChessMove move) {
        TeamColor pieceTeamColor = this.getBoard().getPiece(move.getStartPosition()).getTeamColor();
        if (isMoveCastling(move) && !isInCheck(pieceTeamColor)) {
            if (move.getEndPosition().getColumn() == 7
                && !isInCheckAftermove(new ChessMove(move.getStartPosition(), new ChessPosition(move.getStartPosition().getRow(), 6), null))
            ) {
                if (pieceTeamColor == TeamColor.WHITE) {
                    return !this.whiteKingSideCastlingHasMoved;
                }
                return !this.blackKingSideCastlingHasMoved;
            }
            else if (move.getEndPosition().getColumn() == 3
                && !isInCheckAftermove(new ChessMove(move.getStartPosition(), new ChessPosition(move.getStartPosition().getRow(), 4), null))
            ) {
                if (pieceTeamColor == TeamColor.WHITE) {
                    return !this.whiteQueenSideCastlingHasMoved;
                }
                return !this.blackQueenSideCastlingHasMoved;
            }
        }
        return false;
    }

    /**
     * Determines if the piece at the given position can do an en passant move
     * @param pos
     * @return
     */
    private boolean canDoEnPassant(ChessPosition pos) {
        // Check if the piece is a pawn and is on the correct row
        ChessPiece pawn = this.getBoard().getPiece(pos);
        if (pawn == null) {
            return false;
        }

        int row = pawn.getTeamColor() == TeamColor.WHITE ? 5 : 4;
        if (pawn.getPieceType() == PieceType.PAWN && pos.getRow() == row) {
            // Check if the last move was performed by a pawn and that they moved 2 spaces
            ChessPiece opponentPawn = this.getBoard().getPiece(this.previousMove.getEndPosition());
            if (opponentPawn.getPieceType() == PieceType.PAWN 
                && Math.abs(this.previousMove.getStartPosition().getRow() - this.previousMove.getEndPosition().getRow()) == 2) {

                // Check if the pawns are right next to eachother
                if (Math.abs(pos.getColumn() - this.previousMove.getEndPosition().getColumn()) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the move for a valid enpassant move returns null if there is not a valid move
     * @param move 
     * @return
     */
    private ChessMove getEnPassantMove(ChessPosition pos) {

        if (canDoEnPassant(pos)) {
            ChessPiece pawn = this.getBoard().getPiece(pos);
            int newRow = pawn.getTeamColor() == TeamColor.WHITE ? 6 : 3;
            return new ChessMove(
                    pos, 
                    new ChessPosition(newRow, this.previousMove.getEndPosition().getColumn()), 
                    null
            );
        }
     
        return null;
    }

    /**
     * Determines if a move is an en passant move or not
     * @param move the move to check
     * @return
     */
    private boolean isEnPassantMove(ChessMove move) {
        if (Objects.equals(getEnPassantMove(move.getStartPosition()), (move))) {
            return true;
        }
        return false;

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChessGame) {
            ChessGame other = (ChessGame) o;
            if (this.getBoard().equals(other.getBoard()) && this.getTeamTurn().equals(other.getTeamTurn())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getBoard(), this.getTeamTurn());
    }

}
