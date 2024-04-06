package plu.capstone.playerpiano.subprogram.mainserver.statisticsdb;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.swagger.util.Json;
import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        saveConfig();
    }

    public void loadConfig() {
        CONFIG.loadConfig();
        millisecondsPlayed = CONFIG.getLong(KEY_MILLISECONDS_PLAYED);
        totalSongsPlayed = CONFIG.getLong(KEY_TOTAL_SONGS_PLAYED);
        totalNotesPlayed = CONFIG.getLong(KEY_TOTAL_NOTES_PLAYED);
        totalSustainPedalPressed = CONFIG.getLong(KEY_TOTAL_SUSTAIN_PEDAL_PRESSED);

        //Weirdly even specifying it should be a integer, it still loads as a double
        Map<String, Double> tmp = CONFIG.getMap(KEY_SONGS_PLAYED, String.class, Double.class);
        tmp.forEach((k, v) -> songsPlayed.put(k, v.intValue()));
    }

    private void writeToConfig() {
        CONFIG.setLong(KEY_MILLISECONDS_PLAYED, millisecondsPlayed);
        CONFIG.setLong(KEY_TOTAL_SONGS_PLAYED, totalSongsPlayed);
        CONFIG.setLong(KEY_TOTAL_NOTES_PLAYED, totalNotesPlayed);
        CONFIG.setLong(KEY_TOTAL_SUSTAIN_PEDAL_PRESSED, totalSustainPedalPressed);
        CONFIG.setMap(KEY_SONGS_PLAYED, songsPlayed, String.class, Integer.class); //We want to store all the songs played just incase we want to do something with them later
    }

    public void saveConfig() {
        writeToConfig();
        CONFIG.saveConfig(); //save to file
    }

    //Write to the config file, and return the json object
    public JsonObject toJson() {
        writeToConfig();
        //Copy the object so that the original is not modified
        final JsonObject json = CONFIG.toJsonObject().deepCopy();

        JsonArray songsPlayedJson = new JsonArray();

        //clone the songsPlayed map so that we can sort it
        Map<String, Integer> sortedSongsPlayed = new HashMap<>(songsPlayed);



        //add the top 10 songs played
        sortedSongsPlayed.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(MAX_SONGS_PLAYED)
                .forEach(entry -> {

                    JsonObject song = new JsonObject();
                    song.addProperty("name", entry.getKey());
                    song.addProperty("count", entry.getValue());
                    songsPlayedJson.add(song);

                });

        json.add(KEY_SONGS_PLAYED, songsPlayedJson);

        return json;
    }

}
