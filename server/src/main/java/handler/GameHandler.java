package handler;

import com.google.gson.Gson;

import io.javalin.http.Context;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;

public class GameHandler {

    private GameService gameService;
    Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCreateGame(Context ctx) {
        CreateGameRequest bodyRequest = gson.fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameRequest request = new CreateGameRequest(bodyRequest.gameName(), ctx.header("authorization"));
        CreateGameResult result = this.gameService.createGame(request);
        ctx.status(200).result(gson.toJson(result));
    }

    public void handleListGames(Context ctx) {

    }

    public void handleJoinGames(Context ctx) {
        
    }
}
