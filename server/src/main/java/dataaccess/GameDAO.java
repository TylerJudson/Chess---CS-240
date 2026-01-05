package dataaccess;

import java.util.Collection;

import model.GameData;


public interface GameDAO {
    void createGame(GameData gameData);
    GameData getGame(int gameId);
    Collection<GameData> getAllGames();
    void updateGame(GameData gameData);
}