package server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import results.CreateGameResult;
import results.ListGamesResult;
import results.LoginResult;
import results.RegisterResult;  

import com.google.gson.Gson;

import exceptions.ResponseException;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException {
        var request = buildRequest("POST", "/user", registerRequest);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public LoginResult login(LoginRequest loginRequest) throws ResponseException {
        var request = buildRequest("POST", "/session", loginRequest);
        var response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public CreateGameResult createGame(CreateGameRequest gameRequest, String authToken) throws ResponseException {
        var request = buildRequest("POST", "/game", gameRequest, authToken);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResult.class);
    }

    public ListGamesResult listGames(String authToken) throws ResponseException {
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResult.class);
    }

    public void joinGame(JoinGameRequest joinRequest, String authToken) throws ResponseException {
        var request = buildRequest("PUT", "/game", joinRequest, authToken);
        var result = sendRequest(request);
        handleResponse(result, null);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var result = sendRequest(request);
        handleResponse(result, null);
    }

    public void clearDatabase() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null);
        var result = sendRequest(request);
        handleResponse(result, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
      var request = HttpRequest.newBuilder()
              .uri(URI.create(serverUrl + path))
              .method(method, makeRequestBody(body));
      if (body != null) {
          request.setHeader("Content-Type", "application/json");
      }
      if (authToken != null) {
          request.setHeader("authorization", authToken);
      }
      return request.build();
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        return buildRequest(method, path, body, null);
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
