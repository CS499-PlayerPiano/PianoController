package plugins;

import plu.capstone.playerpiano.controller.logger.ConsoleColors;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.plugin.Plugin;

public class PluginLogger extends Plugin {

    @Override
    public void onNotePlayed(Note note, long timestamp) {
        String onOff = note.isNoteOn() ? ConsoleColors.GREEN_BRIGHT + "ON" : ConsoleColors.RED_BRIGHT + "OFF";

        logger.info(
                ConsoleColors.BLUE + "[" + timestamp + "]" + ConsoleColors.RESET + " Note " + onOff + ConsoleColors.RESET + ", " +
                        ConsoleColors.PURPLE_BRIGHT + note.getNoteName() + note.getOctave() + ConsoleColors.RESET +
                        " key: " + ConsoleColors.PURPLE_BRIGHT + note.getKeyNumber() + ConsoleColors.RESET +
                        " velocity: " + ConsoleColors.PURPLE_BRIGHT + note.getVelocity() + ConsoleColors.RESET
        );
    }
}
