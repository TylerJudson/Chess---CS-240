package service;

public class ApplicationService {
    UserService userService;
    GameService gameService;

    public ApplicationService(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public void clearApplication() {
        this.userService.clearAllData();
        this.gameService.clearAllData();
    }
}
