package plu.capstone.playerpiano.controller.plugins.PluginWebAPI.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import io.swagger.util.Json;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.controller.PlayerPianoController;
import plu.capstone.playerpiano.controller.QueueManager.QueuedSongWithMetadata;
import plu.capstone.playerpiano.controller.plugins.PluginWebAPI.PluginWebAPI;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

public class EndpointControlPiano implements Endpoint {

    private Logger logger = new Logger(this);
    private PluginWebAPI server;
    private JsonArray songDB;

    @Override
    public void register(PluginWebAPI server, Javalin app) {
        this.server = server;
        app.post("/api/control/queue", this::queueSong);
        app.post("/api/control/skip", this::skipSong);
        app.get("/api/control/getQueue", this::getQueue);

        File songDBFile = new File("res" + File.separator + "songs-db" + File.separator + "songs.json");

        try {
            songDB = new Gson().fromJson(Files.readString(songDBFile.toPath()), JsonArray.class);
        } catch (IOException e) {
            logger.error("Error loading song database!");
        }

    }

    private void getQueue(Context context) {
        JsonObject queue = PlayerPianoController.getInstance().getQueueManager().getQueueAsJson();
        context.status(HttpStatus.OK);
        context.result(queue.toString());
    }

    private void skipSong(Context context) {
        //cherck session id
        String sessionUUID = context.sessionAttribute(EndpointsUser.SESSION_UUID);

        if(sessionUUID == null) {
            context.status(HttpStatus.UNAUTHORIZED);
            context.result("No session found! Piano.js must be loaded first!");
            return;
        }

        //TODO: Check if the session ID is the one who queued the song.
        boolean oneWhoQueued = true;

        if(!oneWhoQueued) {
            context.status(HttpStatus.UNAUTHORIZED);
            context.result("You are not the one who queued the song!");
            return;
        }

        server.skipSong();
        context.status(HttpStatus.OK);
        context.result("Success!");
    }

    private void queueSong(Context context) {

        //check session id
        String sessionUUID = context.sessionAttribute(EndpointsUser.SESSION_UUID);

        if(sessionUUID == null) {
            context.status(HttpStatus.UNAUTHORIZED);
            context.result("No session found! Piano.js must be loaded first!");
            return;
        }


        JsonObject body = context.bodyAsClass(JsonObject.class);
        String midiFile = body.get("midiFile").getAsString();

        File songFile = new File("res" + File.separator + "songs-db" + File.separator + "songs" + File.separator + midiFile);
        if(!songFile.exists()) {
            context.status(HttpStatus.NOT_FOUND);
            context.result("Song not found");
            return;
        }

        //Try and find the song in the database
        JsonObject song = null;
        for(JsonElement obj : songDB) {
            if(obj.getAsJsonObject().get("midiFile").getAsString().equals(midiFile)) {
                song = obj.getAsJsonObject();
                break;
            }
        }

        if(song == null) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            context.result("Failed to find song in database, yet it exists in the file system!");
            return;
        }


        try {
            SheetMusic sm = new MidiSheetMusic(songFile);

            int position = server.playSheetMusic(new QueuedSongWithMetadata(sm, song, sessionUUID));

            JsonObject obj = new JsonObject();
            obj.addProperty("position", position);

            obj.addProperty("success", position >= 0);

            if(position == -1) {
                obj.addProperty("error", "Song already exists in queue!");
            }
            else if(position == -2) {
                obj.addProperty("error", "Song is already playing!");
            }

            context.status(HttpStatus.OK);
            context.result(obj.toString());

        }
        catch (InvalidMidiDataException | IOException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            context.result("Error loading song: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
