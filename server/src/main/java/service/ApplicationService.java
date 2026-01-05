package service;

import exceptions.UnauthorizedException;

public class ApplicationService {
    UserService userService;
    GameService gameService;

    public ApplicationService(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void clearApplication(ClearApplicationRequest request) {
        // Verify that the authoken is valid
        if (!this.userService.isAuthorized(request.authToken())) {
            throw new UnauthorizedException("unauthorized");
        }

        this.userService.clearAllData();
        this.gameService.clearAllData();
    }
}
