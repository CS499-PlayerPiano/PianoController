package plugins.PluginWebAPI.endpoints.v1;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plugins.PluginWebAPI.PluginWebAPI;
import plugins.PluginWebAPI.endpoints.Endpoint;

public class V1_ControlPiano implements Endpoint {

    private PluginWebAPI server;

    @Override
    public void register(PluginWebAPI server, Javalin app) {
        this.server = server;
        app.post("/api/v1/control/start", this::startSong);
        app.post("/api/v1/control/stop", this::stopSong);
        app.post("/api/v1/control/pause", this::pauseSong);
        app.post("/api/v1/control/resume", this::resumeSong);
        app.get("/api/v1/control/status", this::getStatus);
        app.post("/api/v1/control/playNotes", this::playNotes);
    }

    private void playNotes(Context context) {
        notImplemented(context);
    }

    private void getStatus(Context context) {
        notImplemented(context);
    }

    private void resumeSong(Context context) {
        notImplemented(context);
    }

    private void pauseSong(Context context) {
        notImplemented(context);
    }

    private void stopSong(Context context) {
        server.stopSheetMusic();
        context.status(HttpStatus.OK);
        context.result("");
    }

    private void startSong(Context context) {
        JsonObject body = context.bodyAsClass(JsonObject.class);
        String songName = body.get("songName").getAsString();

        File songFile = new File("res/songs-db/songs/" + songName);
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
}
