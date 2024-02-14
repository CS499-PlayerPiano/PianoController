package plu.capstone.playerpiano.plugins.impl.PluginWebAPI;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PacketIds {

    CONNECTED("connected"),
    TIMESTAMP("timestamp"),
    NOTES_PLAYED("notesPlayed"),
    SONG_FINISHED("songFinished"),
    SONG_START("songStarted"),
    QUEUE_UPDATED("queueUpdated"),
    SONG_PAUSED("songPaused"),

    ;

    private final String id;

}
