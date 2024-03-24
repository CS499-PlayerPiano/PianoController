package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import io.javalin.Javalin;
import plu.capstone.playerpiano.subprogram.mainserver.SubProgramMainController;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public interface Endpoint {

    void register(JavalinWebServerOutput server, Javalin app);

    default void notImplemented(io.javalin.http.Context ctx) {
        ctx.status(io.javalin.http.HttpStatus.NOT_IMPLEMENTED);
        ctx.result("This endpoint is not implemented yet.");
    }
}
