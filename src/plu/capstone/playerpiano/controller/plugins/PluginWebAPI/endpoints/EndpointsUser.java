package plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;

public class EndpointsUser implements Endpoint {

    private static final String SESSION_CREATE_KEY = "piano-uuid";

    @Override
    public void register(PluginWebAPI server, Javalin app) {

        app.get("/api/users/getSession", this::getOrCreateSession);

    }

    private void getOrCreateSession(Context context) {

        if(context.sessionAttribute(SESSION_CREATE_KEY) == null) {
            context.sessionAttribute(SESSION_CREATE_KEY, java.util.UUID.randomUUID().toString());
        }

        String sessionKey = context.sessionAttribute(SESSION_CREATE_KEY);

        JsonObject response = new JsonObject();
        response.addProperty("session", sessionKey);
        context.result(response.toString());
    }

}
