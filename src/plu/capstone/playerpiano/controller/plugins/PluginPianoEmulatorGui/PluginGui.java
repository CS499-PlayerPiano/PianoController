package plu.capstone.playerpiano.controller.plugins.PluginPianoEmulatorGui;

import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.controller.plugins.PluginPianoEmulatorGui.component.ComponentPiano;
import plu.capstone.playerpiano.sheetmusic.Note;
import plu.capstone.playerpiano.controller.plugin.PluginStateKeeper;
import plu.capstone.playerpiano.sheetmusic.SheetMusicEvent;

/**
 * Plugin to visualize the piano keys.
 */
public class PluginGui extends PluginStateKeeper {

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
        for(Note note : keys) {
            piano.setKeyLit(note);
        }
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        piano.clearLitKeys();
    }

    @Override
    public void onSongFinished(long timestamp) {
        piano.clearLitKeys();
    }
}
