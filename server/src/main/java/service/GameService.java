package service;

import java.util.Collection;

import chess.ChessGame;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import exceptions.BadRequestException;
import exceptions.UnauthorizedException;
import model.GameData;

public class GameService {
    private GameDAO gameDAO;
    private UserService userService;
    private int nextGameId;
    
    public GameService(UserService userService) {
        this.gameDAO = new MemoryGameDAO();
        this.userService = userService;
        this.nextGameId = 1;
    }

    public GameService(GameDAO gameDAO, UserService userService) {
        this.gameDAO = gameDAO;
        this.userService = userService;
        this.nextGameId = 1;
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        // Validate the properties of create game request
        if (request.gameName() == null || request.gameName().isBlank()
            || request.authToken() == null || request.authToken().isBlank()) {
                throw new BadRequestException("bad request");
        }

        // Verify that the authoken is valid
        if (!userService.isAuthorized(request.authToken())) {
            throw new UnauthorizedException("unauthorized");
        }

        // Create the game
        GameData gameData = new GameData(nextGameId, null, null, request.gameName(), new ChessGame());
        this.gameDAO.createGame(gameData);
        nextGameId++;

        return new CreateGameResult(gameData.gameId());
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        return null;
    }

    public void joinGame(JoinGameRequest request) {

    }

    

}
