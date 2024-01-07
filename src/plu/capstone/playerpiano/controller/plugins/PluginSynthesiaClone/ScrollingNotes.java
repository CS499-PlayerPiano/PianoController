package plu.capstone.playerpiano.controller.plugins.PluginSynthesiaClone;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import plu.capstone.playerpiano.controller.utilities.graphics.piano.ComponentPiano;
import plu.capstone.playerpiano.controller.utilities.graphics.piano.KeyShape;
import plu.capstone.playerpiano.sheetmusic.events.Note;

public class ScrollingNotes extends JPanel {

    private int MAX_WIDTH;

    private final ComponentPiano piano;
    public ScrollingNotes(ComponentPiano piano) {
        this.piano = piano;
    }

    private int getXForKeyNumber(int keyNumber) {

        List< KeyShape> keyShape = piano.getKeyShapes();
        Shape shape = keyShape.get(keyNumber).getShape();

        return (int)shape.getBounds2D().getX();

    }

    List<SNote> notesWeAreCurrentlyDrawing = new java.util.ArrayList<>();

    public void onNoteChange(Note[] keys, long timestamp) {

        for(Note note : keys) {

            final int keyNumber = note.toPianoKey();

            //turn note off if we need to
            if(!note.isNoteOn()) {
                synchronized (notesWeAreCurrentlyDrawing) {
                    for (SNote n : notesWeAreCurrentlyDrawing) {
                        if (n.getPianoKeyIndex() == note.toPianoKey()) {
                            n.setNoteOff();
                        }
                    }
                }
            }
            else {
                final int velocity = note.getVelocity();
                final int x = getXForKeyNumber(keyNumber);

                synchronized (notesWeAreCurrentlyDrawing) {
                    notesWeAreCurrentlyDrawing.add(new SNote(
                            keyNumber,
                            velocity,
                            note.isBlackKey(),
                            x
                    ));
                }
            }

        }
        repaint();
    }

    public void scrollDown() {

        synchronized (notesWeAreCurrentlyDrawing) {
            for (SNote note : notesWeAreCurrentlyDrawing) {
                note.scrollDown();
            }
        }

        repaint();

        Set<SNote> tmpSet = new HashSet<>();

        synchronized (notesWeAreCurrentlyDrawing) {
            for (SNote note : notesWeAreCurrentlyDrawing) {
                if (note.areWeOffScreen(getHeight())) {
                    tmpSet.add(note);
                }
            }

            notesWeAreCurrentlyDrawing.removeAll(tmpSet);

        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        //we don't want to draw past the piano pain
        MAX_WIDTH = Math.min(getWidth(), ComponentPiano.getMaxWidth());

        //draw the background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, MAX_WIDTH, getHeight());

        //draw a rainbow gradient DEBUG
//        for(int w = 0; w < MAX_WIDTH; w++) {
//            for(int h = 0; h < getHeight(); h++) {
//                g.setColor(getColorForPosition(w, h));
//                g.fillRect(w, h, 1, 1);
//            }
//        }

        synchronized (notesWeAreCurrentlyDrawing) {
            //Draw the notes
            for (SNote note : notesWeAreCurrentlyDrawing) {
                note.draw((Graphics2D) g);
            }
        }

//        g.setColor(Color.BLACK);
//        g.fillRect(0, 0, getWidth(), getHeight());

    }
}
