package plu.capstone.playerpiano.miscprograms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.Note;

public class FixExistingConfigEntries {

    private static final Gson PRETTY_GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {

        JsonArray oldSongs = PRETTY_GSON.fromJson(Files.readString(new File("res/songs-db/songs.json").toPath()), JsonArray.class);
        JsonArray newSongs = new JsonArray();

        for(int i = 0; i < oldSongs.size(); i++) {
            JsonObject song = oldSongs.get(i).getAsJsonObject();
            System.out.println("Parsing song " + (i + 1) + " of " + oldSongs.size() + ": " + song.get("name").getAsString());
            String midiFile = song.get("midiFile").getAsString();
            File file = new File("res/songs-db/songs/" + midiFile);
            if(!file.exists()) {
                System.out.println("Song not found: " + midiFile);
                continue;
            }

            JsonObject newSong = parseMidiFile(song, file);
            newSongs.add(newSong);
        }

        String prettyJson = PRETTY_GSON.toJson(newSongs);
        Files.writeString(new File("res/songs-db/songs-new.json").toPath(), prettyJson);

    }

    static JsonObject parseMidiFile(JsonObject oldSong, File file) throws InvalidMidiDataException, IOException {
        JsonObject song = oldSong.deepCopy();

        MidiSheetMusic sheetMusic = new MidiSheetMusic(file);
        String midiName = file.getName();
        long noteCount = getNoteCount(sheetMusic, true);
        long songLengthMS = sheetMusic.getSongLengthMS();

        song.addProperty("noteCount", noteCount);
        song.addProperty("songLengthMS", songLengthMS);

        return song;
    }

    static long getNoteCount(MidiSheetMusic sheetMusic, boolean onlyValidKeys) {
        long count = 0;

        for(List<Note> notes : sheetMusic.getNoteMap().values()) {
            for(Note note : notes) {
                if(note.isNoteOn()) {
                    if(onlyValidKeys) {
                        if(note.isValidPianoKey()) {
                            count++;
                        }
                    } else {
                        count++;
                    }
                }
            }
        }

        return count;
    }

//    static JsonObject generateSongDBJson(String midiFile, long noteCount, long songLengthMS) {
//        JsonObject song = new JsonObject();
//        song.addProperty("name", "");
//        song.add("artists", new JsonArray());
//        song.addProperty("midiFile", midiFile);
//        song.addProperty("artwork", "");
//        song.add("tags", new JsonArray());
//        song.addProperty("noteCount", noteCount);
//        song.addProperty("songLengthMS", songLengthMS);
//        return song;
//    }
}
