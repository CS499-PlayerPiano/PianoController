package plugins.gui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.controller.midi.Note;
import plu.capstone.playerpiano.controller.plugin.PluginStateKeeper;
import plugins.gui.component.ComponentPiano;
import plugins.gui.component.ComponentPiano2;

public class PluginGui extends PluginStateKeeper {

    private final JFrame frame = new JFrame("Piano");
    private final ComponentPiano piano = new ComponentPiano();

    private final JFrame frame2 = new JFrame("Piano 2");
    private final ComponentPiano2 piano2 = new ComponentPiano2();

    @Override
    public void onEnable() {
//        frame.add(new JScrollPane(piano));
//        //this.add(new ComponentPiano());
//
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(500, 500);
//
//        SwingUtilities.invokeLater(() -> {
//            frame.setVisible(true);
//        });





        frame2.add(new JScrollPane(piano2));
        //this.add(new ComponentPiano());

        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(500, 500);

        SwingUtilities.invokeLater(() -> {
            frame2.setVisible(true);
        });
    }

    @Override
    public void onNoteChange(boolean[] keys, int[] velocities) {
        for(int i = 0; i < keys.length; i++) {
           piano.setKeyLit(i, keys[i]);
        }
    }

    @Override
    public void onNoteChange2(Note[] keys) {
        for(int i = 0; i < keys.length; i++) {
           piano2.setKeyLit(i, keys[i]);
        }
    }

}
