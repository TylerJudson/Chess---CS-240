package handler;

import com.google.gson.Gson;

import io.javalin.http.Context;
import service.ApplicationService;
import service.ClearApplicationRequest;

public class ApplicationHandler {
    private ApplicationService applicationService;
    Gson gson = new Gson();

    public ApplicationHandler(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void handleClearAppllication(Context ctx) {
        ClearApplicationRequest request = new ClearApplicationRequest(ctx.header("authorization"));
        this.applicationService.clearApplication(request);
        ctx.status(200);
    }
}
