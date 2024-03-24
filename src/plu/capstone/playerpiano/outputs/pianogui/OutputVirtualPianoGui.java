package plu.capstone.playerpiano.outputs.pianogui;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.outputs.OutputStateKeeper;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.utilities.graphics.piano.ComponentPiano;

/**
 * Plugin to visualize the piano keys.
 */
public class OutputVirtualPianoGui extends OutputStateKeeper {

    private final JFrame frame = new JFrame("Virtual Piano Viewer");
    private final ComponentPiano piano = new ComponentPiano();

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
    public void onNoteChange(Note[] keys, long timestamp) {

        for(Note note : keys) {

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

    private static Color getColorForNoteRainbowGradient(Note note) {
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

    private static Color getColorForNoteTrackNumber(Note note) {
        int totalTracks = 16;
        int channel = note.getChannelNum();

        if(channel == Note.NO_CHANNEL) {
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
