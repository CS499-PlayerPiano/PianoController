package plu.capstone.playerpiano.subprogram.mainserver.webserver.endpoints;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.swagger.util.Json;
import java.util.Timer;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.subprogram.mainserver.webserver.JavalinWebServerOutput;

public class EndpointCapstonePresentation implements Endpoint {

    private JavalinWebServerOutput server;

    @Override
    public void register(JavalinWebServerOutput server, Javalin app) {
        this.server = server;
        app.post("/api/capstonedemo/playOneNote", this::playOneNote);
        app.post("/api/capstonedemo/playMultiNotes", this::playNoteNotes);
        app.post("/api/capstonedemo/playDemoSong", this::playDemoSong);
        app.post("/api/capstonedemo/stopDemoSong", this::stopDemoSong);
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

    private void playDemoSong(Context ctx) {
        notImplemented(ctx);
    }

    private void stopDemoSong(Context ctx) {
        notImplemented(ctx);
    }
}
