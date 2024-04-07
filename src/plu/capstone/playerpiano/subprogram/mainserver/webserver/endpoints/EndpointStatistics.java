package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class EndpointStatistics implements Endpoint {

    private JavalinWebServerOutput server;

    @Override
    public void register(JavalinWebServerOutput server, Javalin app) {
        this.server = server;
        app.get("/api/statistics", this::getStatistics);
    }

    private void getStatistics(Context context) {

        JsonObject statsObj = server.getProgram().getStatistics().toJson();

        context.json(statsObj);

    }
    
}
