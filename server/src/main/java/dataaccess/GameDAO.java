package dataaccess;

import java.util.ArrayList;

import model.GameData;


public interface GameDAO {
    void createGame(GameData gameData);
    GameData getGame(int gameID);
    ArrayList<GameData> getAllGames();
    void updateGame(GameData gameData);
    void clearAllData();
}