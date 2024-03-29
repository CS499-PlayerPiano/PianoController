package plu.capstone.playerpiano.plugins.impl.PluginWebAPI.endpoints;

import io.javalin.Javalin;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PluginWebAPI;

public interface Endpoint {

    void register(PluginWebAPI server, Javalin app);

    default void notImplemented(io.javalin.http.Context ctx) {
        ctx.status(io.javalin.http.HttpStatus.NOT_IMPLEMENTED);
        ctx.result("This endpoint is not implemented yet.");
    }
}
