package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import io.javalin.Javalin;

public class EndpointStaticRedirects implements Endpoint{

    @Override
    public void register(PluginWebAPI server, Javalin app) {

        //TODO: Make a config option
        app.get("/request", ctx -> {
            ctx.redirect("https://forms.gle/Buqt29UE6d1iWK4j6");
        });

    }
}
