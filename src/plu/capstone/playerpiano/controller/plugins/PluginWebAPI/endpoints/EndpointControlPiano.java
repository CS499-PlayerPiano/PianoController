package plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiDescription;
import io.javalin.openapi.OpenApiExample;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public class EndpointControlPiano implements Endpoint {

    private PluginWebAPI server;

    @Override
    public void register(PluginWebAPI server, Javalin app) {
        this.server = server;
        app.post("/api/control/start", this::startSong);
        app.post("/api/control/stop", this::stopSong);
        app.post("/api/control/pause", this::pauseSong);
        app.post("/api/control/resume", this::resumeSong);
        app.get("/api/control/status", this::getStatus);
        app.post("/api/control/playNotes", this::playNotes);
    }

    @OpenApi(
            path = "/api/control/playNotes",
            methods = {HttpMethod.POST},
            summary = "Send notes to the piano to be played.",
            description = "Send notes to the piano This endpoint is currently not implemented.",
            tags = {"Piano Control"},
            responses = {
                    @OpenApiResponse(
                            status = "501",
                            description = "This endpoint is not implemented yet.",
                            content = @OpenApiContent(type = "text/plain")
                    ),
            })
    private void playNotes(Context context) {
        notImplemented(context);
    }

    @OpenApi(
            path = "/api/control/status",
            summary = "Return the current status of the piano.",
            description = "Returns the current status of the piano. This endpoint is currently not implemented.",
            tags = {"Piano Control"},
            responses = {
                    @OpenApiResponse(
                            status = "501",
                            description = "This endpoint is not implemented yet.",
                            content = @OpenApiContent(type = "text/plain")
                    ),
            })
    private void getStatus(Context context) {
        notImplemented(context);
    }

    @OpenApi(
            path = "/api/control/resume",
            methods = {HttpMethod.POST},
            summary = "Pause the current song.",
            description = "Pause the current song. This endpoint is currently not implemented.",
            tags = {"Piano Control"},
            responses = {
                    @OpenApiResponse(
                            status = "501",
                            description = "This endpoint is not implemented yet.",
                            content = @OpenApiContent(type = "text/plain")
                    ),
            })
    private void resumeSong(Context context) {
        notImplemented(context);
    }

    @OpenApi(
            path = "/api/control/pause",
            methods = {HttpMethod.POST},
            summary = "Resume the current song.",
            description = "Resume the current song. This endpoint is currently not implemented.",
            tags = {"Piano Control"},
            responses = {
                    @OpenApiResponse(
                            status = "501",
                            description = "This endpoint is not implemented yet.",
                            content = @OpenApiContent(type = "text/plain")
                    ),
            })
    private void pauseSong(Context context) {
        notImplemented(context);
    }

    @OpenApi(
            path = "/api/control/stop",
            methods = {HttpMethod.POST},
            summary = "Stop the current song.",
            description = "Stop the current playing song.",
            tags = {"Piano Control"},
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The song was successfully stopped",
                            content = @OpenApiContent(type = "text/plain")
                    ),
                    @OpenApiResponse(
                            status = "500",
                            description = "Internal Server Error",
                            content = @OpenApiContent(type = "text/plain")
                    )
            })
    private void stopSong(Context context) {
        server.stopSheetMusic();
        context.status(HttpStatus.OK);
        context.result("");
    }

    @OpenApi(
            path = "/api/control/start",
            methods = {HttpMethod.POST},
            summary = "Start a song.",
            description = "Start playing a song.",
            tags = {"Piano Control"},
            requestBody = @OpenApiRequestBody(
                    required = true,
                    description = "Json data of the song we should play",
                    content = {
                            @OpenApiContent(from = SongEntryInput.class)
                    }
            ),
            responses = {
                    @OpenApiResponse(
                            status = "200",
                            description = "The song was successfully started",
                            content = @OpenApiContent(type = "text/plain")
                    ),
                    @OpenApiResponse(
                            status = "404",
                            description = "The song was not found",
                            content = @OpenApiContent(type = "text/plain")
                    ),
                    @OpenApiResponse(
                            status = "500",
                            description = "Internal Server Error",
                            content = @OpenApiContent(type = "text/plain")
                    )
            })
    private void startSong(Context context) {
        JsonObject body = context.bodyAsClass(JsonObject.class);
        String songName = body.get("songName").getAsString();

        File songFile = new File("res" + File.separator + "songs-db" + File.separator + "songs" + File.separator + songName);
        if(!songFile.exists()) {
            context.status(HttpStatus.NOT_FOUND);
            context.result("Song not found");
            return;
        }

        try {
            SheetMusic sm = new MidiSheetMusic(songFile);

            server.playSheetMusic(sm);

            context.status(HttpStatus.OK);
            context.result("");



        }
        catch (InvalidMidiDataException | IOException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            context.result("Error loading song: " + e.getMessage());
            e.printStackTrace();
        }
    }


    class SongEntryInput {

        @OpenApiDescription("The name of the song to play")
        @OpenApiExample("Coconut_Mall.mid")
        public String getSongName() {
            return null;
        }
    }
}
