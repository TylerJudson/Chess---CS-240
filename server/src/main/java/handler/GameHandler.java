package handler;

import com.google.gson.Gson;

import io.javalin.http.Context;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import service.GameService;

public class GameHandler {

    private GameService gameService;
    Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handleCreateGame(Context ctx) {
        CreateGameRequest bodyRequest = gson.fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameRequest request = new CreateGameRequest(bodyRequest.gameName());
        CreateGameResult result = this.gameService.createGame(request, ctx.header("authorization"));
        ctx.status(200).result(gson.toJson(result));
    }

    public void handleListGames(Context ctx) {
        String authToken = ctx.header("authorization");
        ListGamesResult result = this.gameService.listGames(authToken);
        ctx.status(200).result(gson.toJson(result));
    }

    public void handleJoinGames(Context ctx) {
        JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
        String authToken = ctx.header("authorization");
        this.gameService.joinGame(request, authToken);
        ctx.status(200);
    }
}
