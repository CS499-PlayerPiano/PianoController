package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import plu.capstone.playerpiano.subprogram.mainserver.SubProgramMainController;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public interface Endpoint {

    void register(JavalinWebServerOutput server, Javalin app);

    default void notImplemented(io.javalin.http.Context ctx) {
        ctx.status(io.javalin.http.HttpStatus.NOT_IMPLEMENTED);
        JsonObject response = new JsonObject();
        response.addProperty("error", "This endpoint is not implemented yet.");
        ctx.json(response);
    }
}
