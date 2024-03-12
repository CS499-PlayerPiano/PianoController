package plu.capstone.playerpiano.plugins.impl.PluginWebAPI.endpoints;

import io.javalin.Javalin;
import plu.capstone.playerpiano.plugins.impl.PluginWebAPI.PluginWebAPI;

public class EndpointStaticRedirects implements Endpoint{

    @Override
    public void register(PluginWebAPI server, Javalin app) {

        //TODO: Make a config option
        app.get("/request", ctx -> {
            ctx.redirect("https://forms.gle/Buqt29UE6d1iWK4j6");
        });

    }
}
