package plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints.v1;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints.Endpoint;

public class V1_EndpointGetSongData implements Endpoint {

    final File DEFAULT_ALBUM_ART = new File("res/songs-db/artwork/null.jpg");

    @Override
    public void register(PluginWebAPI server, Javalin app) {
        app.get("/api/v1/songs", this::getSongs);
        app.get("/api/v1/album-art/{name}", this::getAlbumArt);
    }

    private void getSongs(Context ctx) throws IOException {
        File file = new File("res/songs-db/songs.json");
        ctx.result(Files.readString(file.toPath()));
    }

    private void getAlbumArt(Context ctx) throws IOException {
        String name = ctx.pathParam("name");
        File result;


        if(name == null || name.isEmpty() || name.equalsIgnoreCase("null")) {
            result = DEFAULT_ALBUM_ART;
        }
        else {
            File file = new File("res/songs-db/artwork/" + name);
            if(!file.exists()) {
                result = DEFAULT_ALBUM_ART;
            }
            else {
                result = file;
            }
        }
        ctx.result(Files.readAllBytes(result.toPath()));
    }
}
