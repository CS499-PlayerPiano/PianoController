package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import io.javalin.Javalin;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class EndpointStaticRedirects implements Endpoint{

    @Override
    public void register(JavalinWebServerOutput server, Javalin app) {

        //TODO: Make a config option
        app.get("/request", ctx -> {
            ctx.redirect("https://forms.gle/Buqt29UE6d1iWK4j6");
        });

    }
}
