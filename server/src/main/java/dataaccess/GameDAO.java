package dataaccess;

import java.util.Collection;

import model.GameData;


public interface GameDAO {
    void createGame(GameData gameData);
    GameData getGame(int gameID);
    Collection<GameData> getAllGames();
    void updateGame(GameData gameData);
    void clearAllData();
}