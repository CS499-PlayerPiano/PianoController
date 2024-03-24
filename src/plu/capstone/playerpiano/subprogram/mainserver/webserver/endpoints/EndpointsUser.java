package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class EndpointsUser implements Endpoint {

    public static final String SESSION_UUID = "piano-uuid";

    @Override
    public void register(JavalinWebServerOutput server, Javalin app) {

        app.get("/api/users/getSession", this::getOrCreateSession);

    }

    private void getOrCreateSession(Context context) {

        if(context.sessionAttribute(SESSION_UUID) == null) {
            context.sessionAttribute(SESSION_UUID, java.util.UUID.randomUUID().toString());
        }

        String sessionKey = context.sessionAttribute(SESSION_UUID);

        JsonObject response = new JsonObject();
        response.addProperty("session", sessionKey);
        context.result(response.toString());
    }

}
