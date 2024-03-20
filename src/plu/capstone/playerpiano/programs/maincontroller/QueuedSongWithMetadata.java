package plu.capstone.playerpiano.programs.maincontroller;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plu.capstone.playerpiano.programs.miditopianofile.MidiCleanerSM;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;

@Getter
public class QueuedSongWithMetadata {
    private final SheetMusic sheetMusic;

    private final JsonObject songDBEntry;
    private final String whoQueued;

    public QueuedSongWithMetadata(SheetMusic sheetMusic, JsonObject songDBEntry, String whoQueued) {
        this.sheetMusic = MidiCleanerSM.applyChanges(sheetMusic); //Apply changes to the sheet music before queuing it.
        this.songDBEntry = songDBEntry;
        this.whoQueued = whoQueued;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QueuedSongWithMetadata && ((QueuedSongWithMetadata) obj).sheetMusic.equals(sheetMusic);
    }

    @Override
    public int hashCode() {
        return sheetMusic.hashCode();
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        obj.addProperty("name", songDBEntry.get("name").getAsString());
        obj.add("artists", songDBEntry.get("artists").getAsJsonArray());
        obj.addProperty("artwork", songDBEntry.get("artwork").getAsString());
        obj.addProperty("lengthMS", sheetMusic.getSongLengthMS());
        obj.addProperty("queuedBy", whoQueued);

        return obj;
    }
}
