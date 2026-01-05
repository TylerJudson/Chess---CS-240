package handler;

import com.google.gson.Gson;

import io.javalin.http.Context;
import service.GameService;

public class GameHandler {

    private GameService gameService;
    Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCreateGame(Context ctx) {

    }

    public void handleListGames(Context ctx) {

    }

    public void handleJoinGames(Context ctx) {
        
    }
}
