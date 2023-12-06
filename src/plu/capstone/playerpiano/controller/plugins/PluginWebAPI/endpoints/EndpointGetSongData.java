package plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.Song;

public class EndpointGetSongData implements Endpoint {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private static final File DEFAULT_ALBUM_ART = new File("res" + File.separator + "songs-db" + File.separator + "artwork" + File.separator + "NULL.jpg");
    private static final File SONGS_DB = new File("res" + File.separator + "songs-db" + File.separator + "songs.json");

    @Override
    public void register(PluginWebAPI server, Javalin app) {
        app.get("/api/songs", this::getSongs);
        app.get("/api/album-art/{name}", this::getAlbumArt);
    }

    @OpenApi(
            path = "/api/songs",
            summary = "Get every song from the database",
            description = "Returns a list of all songs in the database.",
            tags = {"Song Data"},
            responses = {

                    @OpenApiResponse(
                            status = "200",
                            description = "List of songs in JSON format",
                            content = @OpenApiContent(from = Song[].class)
                    ),

                    @OpenApiResponse(
                            status = "500",
                            description = "Internal Server Error"
                    )
            })
    private void getSongs(Context ctx) throws IOException {

        Song[] songs = GSON.fromJson(Files.readString(SONGS_DB.toPath()), Song[].class);

        for(Song song : songs) {
            //TODO: Replace this with the actual number of times the song has been played
            int constantPlayCount = song.getName().hashCode() % 1000;
            constantPlayCount = Math.abs(constantPlayCount);
            song.setTimesPlayed(constantPlayCount);
        }

        ctx.result(GSON.toJson(songs));
    }

    @OpenApi(
            path = "/api/album-art/{name}",
            summary = "Return album art for a song",
            description = "Returns a list of all songs in the database.",
            tags = {"Song Data"},
            pathParams = {
                    @OpenApiParam(name = "name", description = "The name of the song to get album art for", required = true)
            },
            responses = {

                    @OpenApiResponse(
                            status = "200",
                            description = "Return the album art for a song",
                            content = @OpenApiContent(type = "image/jpeg")
                    ),

                    @OpenApiResponse(
                            status = "404",
                            description = "We don't have album art for this song, so we'll return the default album art",
                            content = @OpenApiContent(type = "image/jpeg")
                    ),

                    @OpenApiResponse(
                            status = "500",
                            description = "Internal Server Error",
                            content = @OpenApiContent(type = "text/plain")
                    )
            })
    private void getAlbumArt(Context ctx) throws IOException {
        String name = ctx.pathParam("name");
        File result = null;


        if(name != null && !name.isEmpty() && !name.equalsIgnoreCase("null")) {
            File file = new File("res" + File.separator + "songs-db" + File.separator + "artwork" + File.separator + name);
            if(file.exists()) {
                result = file;
            }
        }

        if(result == null) {
            ctx.status(404);
            result = DEFAULT_ALBUM_ART;
        }

        ctx.header("Content-Type", "image/jpeg");
        ctx.result(Files.readAllBytes(result.toPath()));
    }
}
