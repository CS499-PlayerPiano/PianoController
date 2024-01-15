package plu.capstone.playerpiano.controller.plugins.PluginWebAPI;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PacketIds {

    CONNECTED("connected"),
    TIMESTAMP("timestamp"),
    NOTES_PLAYED("notesPlayed"),
    SONG_FINISHED("songFinished"),
    SONG_START("songStart"),
    QUEUE_UPDATED("queueUpdated"),

    ;

    private final String id;

}
