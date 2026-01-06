package client;

public record ClientResult(ClientType newClient, String authToken, int gameID, String username) {}