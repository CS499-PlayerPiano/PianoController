package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.subprogram.mainserver.QueueError;
import plu.capstone.playerpiano.subprogram.mainserver.QueuedSongWithMetadata;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class EndpointControlPiano implements Endpoint {

    private Logger logger = new Logger(this);
    private JavalinWebServerOutput server;
    private JsonArray songDB;

    @Override
    public void register(JavalinWebServerOutput server, Javalin app) {
        this.server = server;
        app.post("/api/control/queue", this::queueSong);
        app.post("/api/control/skip", this::skipSong);
        app.get("/api/control/getQueue", this::getQueue);
        app.post("/api/control/pause", this::pauseUnpause);

        File songDBFile = new File("res" + File.separator + "songs-db" + File.separator + "songs.json");

        try {
            songDB = new Gson().fromJson(Files.readString(songDBFile.toPath()), JsonArray.class);
        } catch (IOException e) {
            logger.error("Error loading song database!");
        }

    }

    private void pauseUnpause(Context context) {
        boolean success = server.getQueueManager().pauseUnpauseSong();
        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        context.status(HttpStatus.OK);
        context.json(response);
    }

    private void getQueue(Context context) {
        JsonObject queue = server.getQueueManager().getQueueAsJson();
        context.status(HttpStatus.OK);
        context.json(queue);
    }

    private void skipSong(Context context) {
        //cherck session id
        String sessionUUID = context.sessionAttribute(EndpointsUser.SESSION_UUID);

        JsonObject response = new JsonObject();

        if(sessionUUID == null) {
            context.status(HttpStatus.UNAUTHORIZED);
            response.addProperty("success", false);
            response.addProperty("error", "No session found! Piano.js must be loaded first!");
            context.result(response.toString());
            return;
        }

        //TODO: Check if the session ID is the one who queued the song.
        boolean oneWhoQueued = true;

        if(!oneWhoQueued) {
            context.status(HttpStatus.UNAUTHORIZED);
            response.addProperty("success", false);
            response.addProperty("error", "You are not the one who queued the song!");
            context.json(response);
            return;
        }

        server.getQueueManager().stopOrSkipCurrentSong();
        context.status(HttpStatus.OK);
        response.addProperty("success", true);
        context.json(response);
    }

    private void queueSong(Context context) {

        //check session id
        String sessionUUID = context.sessionAttribute(EndpointsUser.SESSION_UUID);
        JsonObject response = new JsonObject();

        if(sessionUUID == null) {
            context.status(HttpStatus.UNAUTHORIZED);
            response.addProperty("success", false);
            response.addProperty("error", "No session found! Piano.js must be loaded first!");
            context.result(response.toString());
            return;
        }


        JsonObject body = context.bodyAsClass(JsonObject.class);
        String midiFile = body.get("midiFile").getAsString();

        File songFile = new File("res" + File.separator + "songs-db" + File.separator + "songs" + File.separator + midiFile);
        if(!songFile.exists()) {
            context.status(HttpStatus.NOT_FOUND);
            response.addProperty("success", false);
            response.addProperty("error", "Song not found!");
            context.json(response);
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
            response.addProperty("success", false);
            response.addProperty("error", "Song not found in database!");
            context.json(response);
            return;
        }


        try {
            SheetMusic sm = new MidiSheetMusic(songFile);

            try {
                int position = server.getQueueManager().queueSong(new QueuedSongWithMetadata(sm, song, sessionUUID));
                response.addProperty("success", true);
                response.addProperty("position", position);
            }
            catch(QueueError e) {
                response.addProperty("success", false);
                response.addProperty("error", e.getMessage());
            }

            context.status(HttpStatus.OK);
            context.json(response);

        }
        catch (InvalidMidiDataException | IOException e) {
            context.status(HttpStatus.INTERNAL_SERVER_ERROR);
            response.addProperty("success", false);
            response.addProperty("error", "Error loading song: " + e.getMessage());
            context.json(response);
            e.printStackTrace();
        }
    }
    
}
