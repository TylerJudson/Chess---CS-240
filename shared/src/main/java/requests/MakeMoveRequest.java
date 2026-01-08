package requests;

import chess.ChessMove;

public record MakeMoveRequest(int gameID, ChessMove move) {}