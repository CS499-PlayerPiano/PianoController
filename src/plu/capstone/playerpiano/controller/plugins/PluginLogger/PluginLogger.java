package plu.capstone.playerpiano.controller.plugins.PluginLogger;

import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;

/**
 * Logs all notes played to the console.
 */
public class PluginLogger extends Plugin {

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
