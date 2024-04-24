package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.swagger.util.Json;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Timer;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.subprogram.mainserver.QueueError;
import plu.capstone.playerpiano.subprogram.mainserver.QueuedSongWithMetadata;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class EndpointCapstonePresentation implements Endpoint {

    private JavalinWebServerOutput server;

    private final Logger logger = new Logger(this);
    private JsonArray songDB;

    @Override
    public void register(JavalinWebServerOutput server, Javalin app) {
        this.server = server;
        app.post("/api/capstonedemo/playOneNote", this::playOneNote);
        app.post("/api/capstonedemo/playMultiNotes", this::playNoteNotes);
        app.post("/api/capstonedemo/playDemoSong", this::playDemoSong);
        app.post("/api/capstonedemo/stopDemoSong", this::stopDemoSong);

        File songDBFile = new File("res" + File.separator + "songs-db" + File.separator + "songs.json");

        try {
            songDB = new Gson().fromJson(Files.readString(songDBFile.toPath()), JsonArray.class);
        } catch (IOException e) {
            logger.error("Error loading song database!");
        }
    }

    private void success(Context ctx) {
        ctx.status(HttpStatus.OK);
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        ctx.json(response);
    }

    private void playOneNote(Context ctx) {
        success(ctx);
        server.getProgram().playNote(new NoteEvent((byte) 60, NoteEvent.MAX_VELOCITY, true));
        new Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                server.getProgram().playNote(new NoteEvent((byte) 60, (byte) 0, false));
            }
        }, 1000);
    }

    private void playNoteNotes(Context ctx) {
        success(ctx);
        server.getProgram().playNote(new NoteEvent((byte) 60, NoteEvent.MAX_VELOCITY, true));
        server.getProgram().playNote(new NoteEvent((byte) 67, NoteEvent.MAX_VELOCITY, true));
        new Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                server.getProgram().playNote(new NoteEvent((byte) 60, (byte) 0, false));
                server.getProgram().playNote(new NoteEvent((byte) 67, (byte) 0, false));
            }
        }, 1000);
    }

    final String DEMO_MIDI_FILE = "CAPSTONE_DEMO.mid";

    private void playDemoSong(Context ctx) {



        String sessionUUID = ctx.sessionAttribute(EndpointsUser.SESSION_UUID);

        JsonObject response = new JsonObject();

        File songFile = new File("res" + File.separator + "songs-db" + File.separator + "songs" + File.separator + DEMO_MIDI_FILE);
        if(!songFile.exists()) {
            ctx.status(HttpStatus.NOT_FOUND);
            response.addProperty("success", false);
            response.addProperty("error", "Song not found!");
            ctx.json(response);
            return;
        }

        JsonObject song = null;
        for(JsonElement obj : songDB) {
            if(obj.getAsJsonObject().get("midiFile").getAsString().equals(DEMO_MIDI_FILE)) {
                song = obj.getAsJsonObject();
                break;
            }
        }

        if(song == null) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            response.addProperty("success", false);
            response.addProperty("error", "Song not found in database!");
            ctx.json(response);
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

            ctx.status(HttpStatus.OK);
            ctx.json(response);

        }
        catch (InvalidMidiDataException | IOException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            response.addProperty("success", false);
            response.addProperty("error", "Error loading song: " + e.getMessage());
            ctx.json(response);
            e.printStackTrace();
        }
    }

    private void stopDemoSong(Context ctx) {
        server.getQueueManager().stopAndClearQueue();

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        ctx.json(response);
    }
}
