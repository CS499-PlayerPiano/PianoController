package plu.capstone.playerpiano.subprogram.mainserver.webserver;

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
    STATISTICS("statistics"),

    ;

    private final String id;

}
