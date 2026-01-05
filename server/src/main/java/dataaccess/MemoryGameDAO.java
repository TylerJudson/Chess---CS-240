package dataaccess;

import java.util.Map;

import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    
    private Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void createGame(GameData gameData) {
        this.games.put(gameData.gameId(), gameData);
    }

    @Override
    public GameData getGame(int gameId) {
        return this.games.get(gameId);
    }

    @Override
    public Collection<GameData> getAllGames() {
        return new ArrayList<>(this.games.values());
    }

    @Override
    public void updateGame(GameData gameData) {
        this.games.put(gameData.gameId(), gameData);
    }
}
