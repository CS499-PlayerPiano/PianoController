package plu.capstone.playerpiano.programs.mididebuggery;

import java.io.File;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

public class MidiDebuggery {

    public static void main(String[] args) {
        try {
            playSongOnJava();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void playSongOnJava() throws Exception {
        Sequence sequence = MidiSystem.getSequence(new File("res/songs-db/songs/Around_the_World_-_Daft_Punk_-_Piano_Version.mid"));

        // Create a sequencer for the sequence
        Sequencer sequencer = MidiSystem.getSequencer();
        sequencer.open();
        sequencer.setSequence(sequence);

        // Start playing
        sequencer.start();
    }

}
