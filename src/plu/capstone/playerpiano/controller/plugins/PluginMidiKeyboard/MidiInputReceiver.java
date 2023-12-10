package plu.capstone.playerpiano.controller.plugins.PluginMidiKeyboard;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import plu.capstone.playerpiano.controller.utilities.MidiMessageDebugger;
import plu.capstone.playerpiano.sheetmusic.events.Note;

public class MidiInputReceiver implements Receiver {

    private final PluginMidiKeyboard pluginMidiKeyboard;

    public MidiInputReceiver(PluginMidiKeyboard pluginMidiKeyboard) {
        this.pluginMidiKeyboard = pluginMidiKeyboard;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {

        //MidiMessageDebugger.printMessage(message);

        if(message instanceof ShortMessage) {

            ShortMessage sm = (ShortMessage) message;

            //Only care about note on and note off messages
            if(sm.getCommand() != ShortMessage.NOTE_ON && sm.getCommand() != ShortMessage.NOTE_OFF) {
                return;
            }

            // This will throw an exception if the message is not a note on or note off message
            Note note = Note.fromMidiMessage(sm);

            //If the ignore velocity option is enabled, set the velocity to 127 for only note
            if(pluginMidiKeyboard.getConfig().getBoolean("ignoreVelocity", false)) {
                if (note.isNoteOn()) {
                    note.setVelocity(127);
                }
                else {
                    //Not actually used for note offs, but its good in case we ever need it
                    note.setVelocity(0);
                }
            }

            //Tell all the plugins that a note was played
            pluginMidiKeyboard.playNote(note);
        }
    }

    @Override
    public void close() {

    }
}
