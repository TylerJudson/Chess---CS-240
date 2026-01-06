package service;

import java.util.Collection;

import chess.ChessGame;
import dataaccess.GameDAO;
import dataaccess.SQLGameDAO;
import exceptions.BadRequestException;
import exceptions.ForbiddenException;
import exceptions.UnauthorizedException;
import model.AuthData;
import model.GameData;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import results.CreateGameResult;
import results.ListGamesResult;

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
        Collection<GameData> games = this.gameDAO.getAllGames();
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
        if ((request.playerColor().equals("WHITE") && gameData.whiteUsername() != null && !gameData.whiteUsername().isBlank())
            || request.playerColor().equals("BLACK") && gameData.blackUsername() != null && !gameData.blackUsername().isBlank()
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

    public void clearAllData() {
        this.gameDAO.clearAllData();
    }
}
