package plu.capstone.playerpiano.miscprograms.songdbverification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

public class SongDBVerification implements Runnable {

    public static void main(String[] args) {new SongDBVerification().run();}

    private final Logger logger = new Logger(this);
    private static final Gson PRETTY_GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    private static final File ROOT_DIR = new File("res/songs-db/");
    private static final File MIDI_DIR = new File(ROOT_DIR,"songs/");
    private static final File ARTWORK_DIR = new File(ROOT_DIR, "artwork/");
    private static final boolean ONLY_VALID_KEYS = true;

    private static final String CHANGE_ME = "CHANGE ME";

    private static final String FIELD_NAME = "name";
    private static final String FIELD_ARTISTS = "artists";
    private static final String FIELD_MIDIFILE = "midiFile";
    private static final String FIELD_ARTWORK = "artwork";
    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_NOTECOUNT = "noteCount";
    private static final String FIELD_SONGLENGTHMS = "songLengthMS";


    /**
     * What this needs to do:
     * 1. Load the songs.json file
     * 2. For each song in the file:
     *     a. Make sure the midi file exists
     *     b. Make sure the album art exists
     *     c. Parse midi for note count
     *     d. Parse midi for song length ms
     *     e. Make sure all fields are present, and certain fields are filled out
     * 3. Sort all songs by name alphabetically
     * 4. Write the new songs.json file beautifully
     * 5. Output any warnings/errors to the console
     */


    @Override
    public void run() {

        try {

            JsonArray oldSongs = PRETTY_GSON.fromJson(Files.readString(new File("res/songs-db/songs.json").toPath()), JsonArray.class);

            List<JsonObject> newSongArray = new ArrayList<>();

            for (int i = 0; i < oldSongs.size(); i++) {
                JsonObject song = oldSongs.get(i).getAsJsonObject();

                if(!isElementAString(song.get(FIELD_NAME))) {
                    logger.warning("Song has no name! Abandoning this config entry.");
                    continue;
                }

                logger.info("Parsing song " + (i + 1) + " of " + oldSongs.size() + ": " + song.get(FIELD_NAME).getAsString());

                //check to see if all fields are present and populated if need be
                checkAndFixSongFields(song);

                //check to make sure the album art exists
                if(doesAlbumArtExist(song.get(FIELD_ARTWORK).getAsString())) {
                    isAlbumArtValid(song.get(FIELD_ARTWORK).getAsString());
                }
                else {
                    logger.warning("  - Song album art does not exist! (" + song.get(FIELD_ARTWORK).getAsString() + ")");
                }

                //check to see if the midi file exists
                //if it does, parse it for note count and song length ms
                if(doesMidiFileExist(song.get(FIELD_MIDIFILE).getAsString())) {
                    // Parse midi for note count and song length ms

                    File midiFile = new File(MIDI_DIR, song.get(FIELD_MIDIFILE).getAsString());
                    SheetMusic sheetMusic = new MidiSheetMusic(midiFile);
                    long songLengthMS = sheetMusic.getSongLengthMS();
                    long noteCount = getTotalNoteCount(sheetMusic);

                    song.addProperty(FIELD_SONGLENGTHMS, songLengthMS);
                    song.addProperty(FIELD_NOTECOUNT, noteCount);
                }
                else {
                    logger.warning("  - Song midi file does not exist! (" + song.get(FIELD_MIDIFILE).getAsString() + ")");
                }

                newSongArray.add(song);
            }

            //Sort new songs by name alphabetically ignoring case
            newSongArray.sort(Comparator.comparing(o -> o.get(FIELD_NAME).getAsString().toLowerCase()));

            //Write the new songs.json file beautifully
            String prettyJson = PRETTY_GSON.toJson(newSongArray);
            Files.writeString(new File(ROOT_DIR, "songs.json").toPath(), prettyJson);

        }
        catch(Exception e) {
            logger.error(e.getMessage());
        }
    }

    private boolean isAlbumArtValid(String path) {

        File file = new File(ARTWORK_DIR, path);
        if(!file.exists()) {
            logger.warning("  - Song album art does not exist! (" + path + ")");
            return false;
        }

        //check the file extension
        String extension = path.substring(path.lastIndexOf('.') + 1);
        if(!extension.equalsIgnoreCase("jpg")) {
            logger.warning("  - Song album art is not a jpg! Currently this isn't a mandatory change, but will be informed in the future! (" + path + ")");
        }

        //if it exists, check to make sure it's a buffered image
        try {
            BufferedImage image = ImageIO.read(file);

            if(image == null) {
                logger.warning("  - Song album art is not a valid image! (" + path + ")");
                return false;
            }

            if(image.getWidth() != image.getHeight()) {
                logger.warning("  - Song album art is not a square! (" + path + " - " + image.getWidth() + "x" + image.getHeight() + ")");
                return false;
            }

        }
        catch (IOException e) {
            logger.warning("  - Song album art is not a valid image! (" + path + ")");
            return false;
        }

        return true;
    }

    private long getTotalNoteCount(SheetMusic sheetMusic) {
        long count = 0;

        for(List<SheetMusicEvent> events : sheetMusic.getEventMap().values()) {
            for(SheetMusicEvent event : events) {
                if(event instanceof Note) {
                    Note note = (Note) event;
                    if (note.isNoteOn()) {
                        if (ONLY_VALID_KEYS) {
                            if (note.isValidPianoKey()) {
                                count++;
                            }
                        } else {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    private boolean doesMidiFileExist(String midiFileName) {
        return doesFilenameExistCaseSensitive(MIDI_DIR, midiFileName);
    }

    private boolean doesAlbumArtExist(String path) {
        return doesFilenameExistCaseSensitive(ARTWORK_DIR, path);
    }

    //Linux is case-sensitive, windows is not, so our code needs to be case-sensitive
    private boolean doesFilenameExistCaseSensitive(File dir, String filename) {
        String[] files = dir.list();
        for(String file : files)
            if(file.equals(filename))
                return true;
        return false;
    }

    private boolean checkAndFixSongFields(JsonObject song) {

        boolean result = true;



        if(!isElementAnArray(song.get(FIELD_ARTISTS), false)) {
            logger.warning("  - Song has no artist list! Creating....");
            JsonArray artists = new JsonArray();
            artists.add(CHANGE_ME);
            song.add("artists", artists);
            result = false;
        }

        if(!isElementAString(song.get(FIELD_MIDIFILE))) {
            logger.warning("  - Song has no midi file field! Creating....");
            song.addProperty("midiFile", CHANGE_ME);
            result = false;
        }

        if(!isElementAString(song.get(FIELD_ARTWORK))) {
            logger.warning("  - Song has no artwork field! Creating....");
            song.addProperty("artwork", CHANGE_ME);
            result = false;
        }

        if(!isElementAnArray(song.get(FIELD_TAGS), true)) {
            logger.warning("  - Song has no tags field! Creating....");
            song.add("tags", new JsonArray());
            result = false;
        }

        return result;
    }

    private boolean isElementAString(JsonElement element) {

        if(element == null) return false;
        if(element.isJsonNull()) return false;
        if(!element.isJsonPrimitive()) return false;
        if(!element.getAsJsonPrimitive().isString()) return false;

        return true;
    }

    private boolean isElementAnArray(JsonElement element, boolean allowEmpty) {
        if(element == null) return false;
        if(element.isJsonNull()) return false;
        if(!element.isJsonArray()) return false;
        if(!allowEmpty && element.getAsJsonArray().size() == 0) return false;
        return true;
    }
}
