package service;

import java.util.Collection;

import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;

public class GameService {
    private GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new MemoryGameDAO();
    }
    
    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        return null;
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        return null;
    }

    public void joinGame(JoinGameRequest request) {

    }

}
