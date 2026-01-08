package service;

import java.util.ArrayList;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessGame.TeamColor;
import dataaccess.GameDAO;
import dataaccess.SQLGameDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import model.AuthData;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LeaveGameRequest;
import requests.MakeMoveRequest;
import requests.ResignGameRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.MakeMoveResult;

public class GameService {
    private GameDAO gameDAO;
    private UserService userService;
    private int nextGameId;
    
    public GameService(UserService userService) {
        this.gameDAO = new SQLGameDAO();
        this.userService = userService;
        this.nextGameId = 1;
    }

    public GameService(GameDAO gameDAO, UserService userService) {
        this.gameDAO = gameDAO;
        this.userService = userService;
        this.nextGameId = 1;
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) {
        // Validate the properties of create game request
        if (request.gameName() == null || request.gameName().isBlank()
            || authToken == null || authToken.isBlank()) {
                throw new BadRequestException("bad request");
        }

        // Verify that the authoken is valid
        if (!this.userService.isAuthorized(authToken)) {
            throw new UnauthorizedException("unauthorized");
        }

        // Create the game
        GameData gameData = new GameData(nextGameId, null, null, request.gameName(), new ChessGame());
        this.gameDAO.createGame(gameData);
        nextGameId++;

        return new CreateGameResult(gameData.gameID());
    }

    public ListGamesResult listGames(String authToken) {
        // Verify the authToken
        if (!this.userService.isAuthorized(authToken)) {
            throw new UnauthorizedException("unauthorized");
        }

        // Get the list of games
        ArrayList<GameData> games = this.gameDAO.getAllGames();
        return new ListGamesResult(games);
    }

    public void joinGame(JoinGameRequest request, String authToken) {
        // Validate the properties of request
        if (request.playerColor() == null || !(request.playerColor().equals("WHITE") || request.playerColor().equals("BLACK"))
            || authToken == null || authToken.isBlank()
        ) {
            throw new BadRequestException("bad request");
        }

        // Verify that the authoken is valid
        AuthData authData = this.userService.getAuthData(authToken);
        if (authData == null) {
            throw new UnauthorizedException("unathorized");
        }

        // Verify that the game exists
        GameData gameData = this.gameDAO.getGame(request.gameID());
        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        // Check to see if the color is already in use
        if ((
                request.playerColor().equals("WHITE") 
                && gameData.whiteUsername() != null 
                && !gameData.whiteUsername().isBlank() 
                && !authData.username().equals(gameData.whiteUsername())
            )
            || request.playerColor().equals("BLACK") 
            && gameData.blackUsername() != null 
            && !gameData.blackUsername().isBlank() 
            && !authData.username().equals(gameData.blackUsername())
        ) {
            throw new ForbiddenException("color already taken");
        }

        // Update the game
        if (request.playerColor().equals("WHITE")) {
            GameData newGameData = new GameData(gameData.gameID(), authData.username(), gameData.blackUsername(), gameData.gameName(), gameData.game());
            this.gameDAO.updateGame(newGameData);
        }
        else {
            GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
            this.gameDAO.updateGame(newGameData);
        }
    }

    public void leaveGame(LeaveGameRequest request, String authToken) {
        // validate the request
        if (request == null) {
            throw new BadRequestException("bad request");
        }

        // verify the authtoken
        AuthData authData = userService.getAuthData(authToken);
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // verify the gameID
        GameData gameData = gameDAO.getGame(request.gameID());
        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        // Update the game
        if (gameData.whiteUsername() != null && gameData.whiteUsername().equals(authData.username())) {
            GameData newGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
            gameDAO.updateGame(newGameData);
        }
        else if (gameData.blackUsername() != null && gameData.blackUsername().equals(authData.username())) {
            GameData newGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
            gameDAO.updateGame(newGameData);
        }
    }

    public void resign(ResignGameRequest request, String authToken) {
        // validate the request
        if (request == null) {
            throw new BadRequestException("bad request");
        }

        // verify the authtoken
        AuthData authData = userService.getAuthData(authToken);
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // verify the gameID
        GameData gameData = gameDAO.getGame(request.gameID());
        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        // verify that the user is actually apart of the game
        if (!gameData.whiteUsername().equals(authData.username()) && !gameData.blackUsername().equals(authData.username())) {
            throw new ForbiddenException("you cannot resign the game");
        }

        // verify that the game isn't already over
        if (gameData.game().getGameOver()) {
            throw new ForbiddenException("the game is already over");
        }

        // update the game to the game over status
        gameData.game().setGameOver(true);

        gameDAO.updateGame(gameData);
    }

    public MakeMoveResult makeMove(MakeMoveRequest request, String authToken) {
        // validate the request
        if (request == null || request.move() == null) {
            throw new BadRequestException("bad request");
        }

        // verify the authtoken
        AuthData authData = userService.getAuthData(authToken);
        if (authData == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // check to make sure the game exists
        GameData gameData = this.gameDAO.getGame(request.gameID());
        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        // check to make sure the user can move the piece

        ChessGame game = gameData.game();
        ChessPiece piece = game.getBoard().getPiece(request.move().getStartPosition());
        if (game.getGameOver()) {
            throw new ForbiddenException("the game is over");
        }

        if (piece == null) {
            throw new BadRequestException("invalid start position");
        }

        // the username has to match the gamedata's username of the color the piece is moving
        if (piece.getTeamColor() == TeamColor.WHITE && !authData.username().equals(gameData.whiteUsername())
            || piece.getTeamColor() == TeamColor.BLACK && !authData.username().equals(gameData.blackUsername())) {
                throw new ForbiddenException("you cannot move this piece");
        }

        // The user has to match the current team's turn
        if (game.getTeamTurn() == TeamColor.WHITE && !authData.username().equals(gameData.whiteUsername())
            || game.getTeamTurn() == TeamColor.BLACK && !authData.username().equals(gameData.blackUsername())) {
                throw new ForbiddenException("it is not your turn");
        }

        try {
            game.makeMove(request.move());
        }
        catch (Exception ex) {
            throw new ForbiddenException("invalid move");
        }

        gameDAO.updateGame(gameData);

        return new MakeMoveResult(gameData);
    }

    public GameData getGame(int gameID, String authToken) {
        // Verify that the authoken is valid
        if (!this.userService.isAuthorized(authToken)) {
            throw new UnauthorizedException("unauthorized");
        }

        // Verify that the game exists
        GameData gameData = this.gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new BadRequestException("bad request");
        }

        return gameData;
    }

    public void clearAllData() {
        this.gameDAO.clearAllData();
    }
}
