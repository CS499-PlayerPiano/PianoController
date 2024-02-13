package plu.capstone.playerpiano.plugins.impl.PluginSynthesiaClone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.plugins.plugin.PluginStateKeeper;
import plu.capstone.playerpiano.utilities.graphics.piano.ComponentPiano;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;

/**
 * Plugin to visualize the piano keys.
 */
public class PluginSynthesiaGui extends PluginStateKeeper {

    private final JFrame frame = new JFrame("Synthesia Viewer");
    private final ComponentPiano piano = new ComponentPiano();
    private final ScrollingNotes scrollingNotes = new ScrollingNotes(piano);

    @Override
    public void onEnable() {

        piano.setBackgroundColor(Color.DARK_GRAY);

        JPanel scrollPanel = new JPanel();

        scrollPanel.setLayout(new BorderLayout());
        scrollPanel.add(piano, java.awt.BorderLayout.SOUTH);
        scrollPanel.add(scrollingNotes, BorderLayout.CENTER);

        frame.add(new JScrollPane(scrollPanel));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });
    }


    @Override
    public void onNoteChange(Note[] keys, long timestamp) {

        scrollingNotes.onNoteChange(keys, timestamp);

        for(Note note : keys) {
            piano.setKeyLit(note, getColorForNoteRainbowGradient(note));
        }
    }

    @Override
    public void onTimestampEvent(long current, long end) {
        scrollingNotes.scrollDown();
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
