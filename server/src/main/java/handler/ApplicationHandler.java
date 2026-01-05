package handler;

import com.google.gson.Gson;

import io.javalin.http.Context;
import service.ApplicationService;

public class ApplicationHandler {
    private ApplicationService applicationService;
    Gson gson = new Gson();

    public ApplicationHandler(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void handleClearAppllication(Context ctx) {
        this.applicationService.clearApplication();
        ctx.status(200);
    }
}
