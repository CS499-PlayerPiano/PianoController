package plu.capstone.playerpiano.outputs.pianogui;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.outputs.OutputStateKeeper;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.utilities.graphics.piano.ComponentPiano;

/**
 * Plugin to visualize the piano keys.
 */
public class OutputVirtualPianoGui extends OutputStateKeeper {

    private JFrame frame;
    private ComponentPiano piano;

    private ColorMode colorMode;

    public enum ColorMode {
        RAINBOW_GRADIENT,
        TRACK_NUMBER,
    }

    @Override
    public String getName() {
        return "Virtual Piano GUI";
    }

    @Override
    public void onEnable() {

        frame = new JFrame("Virtual Piano Viewer");
        piano = new ComponentPiano();

        piano.setBackgroundColor(Color.BLUE);
        frame.add(new JScrollPane(piano));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });

        colorMode = getConfig().getEnum("colorMode", ColorMode.class);
    }

    @Override
    public void setDefaultConfigValues() {
        getConfig().setEnum("colorMode", ColorMode.RAINBOW_GRADIENT);
    }

    @Override
    public void onNoteChange(NoteEvent[] keys, long timestamp) {

        for(NoteEvent note : keys) {

            Color color;

            if(colorMode == ColorMode.TRACK_NUMBER) {
                color = getColorForNoteTrackNumber(note);
            }
            else if(colorMode == ColorMode.RAINBOW_GRADIENT) {
                color = getColorForNoteRainbowGradient(note);
            }
            else {
                getLogger().warning("Invalid color mode: " + getConfig().getString("colorMode"));
                color = Color.RED;
            }

            piano.setKeyLit(note, color);
        }
    }

    @Override
    public void onPause() {
        piano.clearLitKeys();
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        piano.clearLitKeys();
    }

    @Override
    public void onSongFinished(long timestamp) {
        piano.clearLitKeys();
    }

    private static Color getColorForNoteRainbowGradient(NoteEvent note) {
        int totalNotes = 88;
        int key = note.getKeyNumber();

        float percent = (float)key / totalNotes;
        if(percent > 1) {
            percent = 1;
        }

        if(percent < 0) {
            percent = 0;
        }

        return Color.getHSBColor(percent, 1, 1);
    }

    private static Color getColorForNoteTrackNumber(NoteEvent note) {
        int totalTracks = 16;
        int channel = note.getChannelNum();

        if(channel == NoteEvent.NO_CHANNEL) {
            return Color.RED;
        }

        float percent = (float)channel / totalTracks;
        if(percent > 1) {
            percent = 1;
        }

        if(percent < 0) {
            percent = 0;
        }

        return Color.getHSBColor(percent, 1, 1);
    }
}
