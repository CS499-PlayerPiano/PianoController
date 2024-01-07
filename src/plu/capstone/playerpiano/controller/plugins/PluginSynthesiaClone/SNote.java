package plu.capstone.playerpiano.controller.plugins.PluginSynthesiaClone;

import java.awt.Color;
import java.awt.Graphics2D;
import lombok.Getter;
import lombok.Setter;
import plu.capstone.playerpiano.controller.utilities.graphics.piano.ComponentPiano;

public class SNote {

    @Getter private int pianoKeyIndex;
    private int velocity;

    @Getter
    private final int x;

    @Getter @Setter private int y = 0;

    @Getter private final int width;
    @Getter @Setter private int height = 0;

    public SNote(int pianoKeyIndex, int velocity, boolean blackKey, int x) {
        this.pianoKeyIndex = pianoKeyIndex;
        this.velocity = velocity;

        this.x = x;
        this.width = blackKey ? (int) ComponentPiano.BLACK_KEY_WIDTH : ComponentPiano.WHITE_KEY_WIDTH;

    }

    public void draw(Graphics2D g2d) {

        //draw the note
        g2d.setColor(getNoteColor());
        g2d.fillRect(x, y, width, height);

    }

    boolean noteOn = true;
    public void setNoteOff() {
        noteOn = false;
    }

    /*
    if note is on, increase height while keeping y the same
    if note is off, move note down
     */
    public void scrollDown() {
        if(noteOn) {
            height++;
        }
        else {
            y++;
        }

    }

    public boolean areWeOffScreen(int maxHeightOfBackground) {
        return y > maxHeightOfBackground;
    }

    private Color getNoteColor() {

        float percent = (float)pianoKeyIndex / 88;
        if(percent > 1) {
            percent = 1;
        }

        if(percent < 0) {
            percent = 0;
        }

        return Color.getHSBColor(percent, 1, 1);
    }

}
