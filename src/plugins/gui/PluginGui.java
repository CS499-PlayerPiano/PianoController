package plugins.gui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.plugin.PluginStateKeeper;
import plugins.gui.component.ComponentPiano;

public class PluginGui extends PluginStateKeeper {

    private static final int TOTAL_KEYS = 88;

    private final JFrame frame = new JFrame("Piano");
    private final ComponentPiano piano = new ComponentPiano(this);

    public enum ColorMode {
        RAINBOW_GRADIENT,
        TRACK_NUMBER,
    }

    @Override
    public void onEnable() {

        frame.add(new JScrollPane(piano));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });
    }

    @Override
    public void setDefaultConfigValues() {
        config.setEnum("colorMode", ColorMode.RAINBOW_GRADIENT);
    }

    @Override
    public void onNoteChange(Note[] keys, long timestamp) {

        //Valid notes are from 21 to 108 in MIDI
        //https://www.inspiredacoustics.com/en/MIDI_note_numbers_and_center_frequencies
//        for(int i = 21; i < 21 + TOTAL_KEYS; i++) {
//            final int key = i - 21;
//            piano.setKeyLit(key, keys[key]);
//        }

        for(Note note : keys) {
            piano.setKeyLit(note);
        }

    }

}
