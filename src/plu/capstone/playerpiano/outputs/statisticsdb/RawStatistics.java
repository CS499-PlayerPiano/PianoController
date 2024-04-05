package plu.capstone.playerpiano.outputs.statisticsdb;

import com.google.gson.JsonObject;
import io.swagger.util.Json;
import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.plaf.nimbus.State;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plu.capstone.playerpiano.JsonConfigWrapper;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;

@NoArgsConstructor
public class RawStatistics {

    private final JsonConfigWrapper CONFIG = new JsonConfigWrapper(new File("res/statistics.json"));
    private static final String KEY_MILLISECONDS_PLAYED = "millisecondsPlayed";
    private static final String KEY_TOTAL_SONGS_PLAYED = "totalSongsPlayed";
    private static final String KEY_TOTAL_NOTES_PLAYED = "totalNotesPlayed";
    private static final String KEY_TOTAL_SUSTAIN_PEDAL_PRESSED = "totalSustainPedalPressed";
    private static final String KEY_SONGS_PLAYED = "songsPlayed";

    //TODO: Config
    private static final int MAX_SONGS_PLAYED = 10;

    private long millisecondsPlayed;
    private long totalSongsPlayed;
    private long totalNotesPlayed;
    private long totalSustainPedalPressed;

    private Map<String, Integer> songsPlayed = new HashMap<>();

    public void incrementTotalNotesPlayed() {totalNotesPlayed ++;}
    private void incrementTotalSongsPlayed() {totalSongsPlayed ++;}
    public void incrementTotalSustainPedalPressed() {totalSustainPedalPressed ++;}
    public void incrementMillisecondsPlayed(long milliseconds) {millisecondsPlayed += milliseconds;}

    public void onSongPlayed(String songName) {
        incrementTotalSongsPlayed();
        songsPlayed.put(songName, songsPlayed.getOrDefault(songName, 0) + 1);
    }

    public void read() {
        CONFIG.loadConfig();
        millisecondsPlayed = CONFIG.getLong(KEY_MILLISECONDS_PLAYED);
        totalSongsPlayed = CONFIG.getLong(KEY_TOTAL_SONGS_PLAYED);
        totalNotesPlayed = CONFIG.getLong(KEY_TOTAL_NOTES_PLAYED);
        totalSustainPedalPressed = CONFIG.getLong(KEY_TOTAL_SUSTAIN_PEDAL_PRESSED);

        songsPlayed = CONFIG.getMap(KEY_SONGS_PLAYED, String.class, Integer.class);
    }

    private void writeToConfig() {
        CONFIG.setLong(KEY_MILLISECONDS_PLAYED, millisecondsPlayed);
        CONFIG.setLong(KEY_TOTAL_SONGS_PLAYED, totalSongsPlayed);
        CONFIG.setLong(KEY_TOTAL_NOTES_PLAYED, totalNotesPlayed);
        CONFIG.setLong(KEY_TOTAL_SUSTAIN_PEDAL_PRESSED, totalSustainPedalPressed);
        CONFIG.setMap(KEY_SONGS_PLAYED, songsPlayed); //We want to store all the songs played just incase we want to do something with them later
    }

    public void write() {
        writeToConfig();
        CONFIG.saveConfig(); //save to file
    }

    //Write to the config file, and return the json object
    public JsonObject toJson() {
        writeToConfig();
        //Copy the object so that the original is not modified
        final JsonObject json = CONFIG.toJsonObject().deepCopy();

        JsonObject songsPlayedJson = new JsonObject();
        //add the top 10 songs played
        songsPlayed.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(MAX_SONGS_PLAYED)
                .forEach(entry -> songsPlayedJson.addProperty(entry.getKey(), entry.getValue()));

        json.add(KEY_SONGS_PLAYED, songsPlayedJson);

        return CONFIG.toJsonObject();
    }

}
