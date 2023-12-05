package plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;

public class TestEndpoint implements Endpoint{
    @Override
    public void register(PluginWebAPI server, Javalin app) {


        app.get("/api/test", this::test);
    }

    @OpenApi(
            path = "/test",
            summary = "Test Endpoint",
            description = "This is a test endpoint.",
            tags = {"Test"},
            queryParams = {
                    @OpenApiParam(name = "test", description = "Test query parameter", required = true, allowEmptyValue = true, deprecated = true)
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Hello World!")
            })
    private void test(Context context) {
        context.result("Hello World!");
    }
}
