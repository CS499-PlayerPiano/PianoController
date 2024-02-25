package plu.capstone.playerpiano.plugins.impl.PluginLogger;

import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.plugins.Plugin;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

/**
 * Logs all notes played to the console.
 */
public class PluginLogger extends Plugin {

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        logger.info("Song Started");
    }

    @Override
    public void onSongFinished(long timestamp) {
        logger.info("Song Finished");
    }

    @Override
    public void onPause() {
        logger.info("Song Paused");
    }

    @Override
    public void onUnpause() {
        logger.info("Song Unpaused");
    }

    /**
     * Every time a note is played, we log it to the console with some pretty colors
     * @param note the note that was played
     * @param timestamp the timestamp of the note
     */
    @Override
    public void onNotePlayed(Note note, long timestamp) {
        String onOff = note.isNoteOn() ? ConsoleColors.GREEN_BRIGHT + "ON" : ConsoleColors.RED_BRIGHT + "OFF";

        logger.info(
                ConsoleColors.BLUE + "[" + timestamp + "]" + ConsoleColors.RESET + " Note " + onOff + ConsoleColors.RESET + ", " +
                        ConsoleColors.PURPLE_BRIGHT + note.getNoteName() + note.getOctave() + ConsoleColors.RESET +
                        " key: " + ConsoleColors.PURPLE_BRIGHT + note.getKeyNumber() + ConsoleColors.RESET +
                        " velocity: " + ConsoleColors.PURPLE_BRIGHT + note.getVelocity() + ConsoleColors.RESET +
                        " channel: " + ConsoleColors.PURPLE_BRIGHT + note.getChannelNum() + ConsoleColors.RESET
        );
    }
}
