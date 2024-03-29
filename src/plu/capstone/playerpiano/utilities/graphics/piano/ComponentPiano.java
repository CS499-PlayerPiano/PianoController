package plu.capstone.playerpiano.utilities.graphics.piano;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;


public class ComponentPiano extends JComponent {

    private static final float WHITE_KEY_ASPECT = (7f / 8f) / (5.7f);
    private static final float BLACK_KEY_HEIGHT = 3.5f / 6f;

    private static final EnumNotes FIRST_NOTE = EnumNotes.A;
    private static final int WHITE_KEY_COUNT = 52;
    public static final int WHITE_KEY_WIDTH = Math.round(220 * WHITE_KEY_ASPECT);
    public static final float BLACK_KEY_WIDTH = (WHITE_KEY_WIDTH * 14f / 24);

    private static final int WHITE_KEY_HEIGHT = 220;

    private List<KeyShape> keyShapes;
    private final Map<Integer, Color> litKeys = new HashMap<>();

    private Color COLOR_BACKGROUND = Color.BLUE;
    private static final Color COLOR_WHITE_KEY = Color.WHITE;
    //    private static final Color COLOR_WHITE_KEY_LIT = new Color(0xDF3030);
    private static final Color[] COLOR_WHITE_KEY_GRADIENT = {
            new Color(0x60FFFFFF, true),
            new Color(0x00FFFFFF, true),
            new Color(0x00FFFFFF, true),
            new Color(0x60FFFFFF, true),
    };
    private static final float[] WHITE_KEY_GRADIENT_DISTRIBUTIONS = { 0, 0.2f, 0.8f, 1 };
    private static final Color COLOR_BLACK_KEY = Color.BLACK;
    //    private static final Color COLOR_BLACK_KEY_LIT = new Color(0xFF5050);
    private static final Color[] COLOR_BLACK_KEY_GRADIENT = {
            new Color(0xA0000000, true),
            new Color(0x30000000, true),
            new Color(0x00000000, true),
            new Color(0x00000000, true),
            new Color(0x30000000, true),
    };
    private static final float[] BLACK_KEY_GRADIENT_DISTRIBUTIONS = { 0, 0.02f, 0.125f, 0.975f, 1 };

    private static final float KEY_BEVEL = 0.15f;

    public void setBackgroundColor(Color bg) {
        this.COLOR_BACKGROUND = bg;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        keyShapes = null;
    }

    public List<KeyShape> getKeyShapes() {
        if (keyShapes == null) {
            keyShapes = generateKeyShapes();
        }
        return keyShapes;
    }

    public static int getMaxWidth() {
        return WHITE_KEY_COUNT * WHITE_KEY_WIDTH;
    }

    private List<KeyShape> generateKeyShapes() {
        List<KeyShape> shapes = new ArrayList<>();

        int x = 0;
        EnumNotes note = FIRST_NOTE;
        for (int w = 0; w < WHITE_KEY_COUNT; w++) {
            float cutLeft = note.getCutLeft(), cutRight = note.getCutRight();

            if (w == 0) {
                cutLeft = 0;
            }
            if (w == WHITE_KEY_COUNT - 1) {
                cutRight = 0;
            }

            shapes.add(new KeyShape(createWhiteKey(x, cutLeft, cutRight), false));

            if (cutRight != 0) {
                shapes.add(new KeyShape(createBlackKey(x + WHITE_KEY_WIDTH - (WHITE_KEY_WIDTH * cutRight)), true));
            }

            x += WHITE_KEY_WIDTH;

            note = note.next();
        }

        return Collections.unmodifiableList(shapes);
    }

    private Shape createWhiteKey(float x, float cutLeft, float cutRight) {
        float width = WHITE_KEY_WIDTH, height = WHITE_KEY_HEIGHT;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(x + cutLeft * width, 0);
        path.lineTo(x + width - (width * cutRight), 0);
        if (cutRight != 0) {
            path.lineTo(x + width - (width * cutRight), height * BLACK_KEY_HEIGHT);
            path.lineTo(x + width, height * BLACK_KEY_HEIGHT);
        }

        path.lineTo(x + width, height - (width * KEY_BEVEL) - 1);
        path.quadTo(x + width, height, x + width * (1 - KEY_BEVEL), height - 1);
        path.lineTo(x + width * KEY_BEVEL, height - 1);
        path.quadTo(x, height, x, height - (width * KEY_BEVEL) - 1);
        if (cutLeft != 0) {
            path.lineTo(x, height * BLACK_KEY_HEIGHT);
            path.lineTo(x + width * cutLeft, height * BLACK_KEY_HEIGHT);
        }
        path.closePath();
        return path;
    }


    private Shape createBlackKey(float x) {
        return new Rectangle2D.Float(
                x, 0,
                WHITE_KEY_WIDTH * 14f / 24,
                WHITE_KEY_HEIGHT * BLACK_KEY_HEIGHT
        );
    }


    @Override
    public void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D)g1;
        Rectangle clipRect = g.getClipBounds();

        g.setColor(COLOR_BACKGROUND);
        g.fill(clipRect);

        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1f));

        List<KeyShape> keyShapes = getKeyShapes();
        for (int i = 0; i < keyShapes.size(); i++) {
            KeyShape ks = keyShapes.get(i);
            Rectangle bounds = ks.getShape().getBounds();
            if (!bounds.intersects(clipRect)) continue;

            Color color = ks.isBlack() ? COLOR_BLACK_KEY : COLOR_WHITE_KEY;
            if(isKeyLit(i)) {
                color = litKeys.get(i);
            }

            g.setColor(color);
            g.fill(ks.getShape());

            //Draw gradient
            if(ks.isBlack()) {
                bounds.setRect(
                        bounds.getX() + bounds.getWidth() * 0.15f,
                        bounds.getY() + bounds.getHeight() * 0.03f,
                        bounds.getWidth() * 0.7f,
                        bounds.getHeight() * 0.97f
                );
                g.setPaint(new GradientPaint(
                        bounds.x, bounds.y, COLOR_WHITE_KEY_GRADIENT[0],
                        bounds.x, bounds.y + bounds.height * 0.5f, COLOR_WHITE_KEY_GRADIENT[1]
                ));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 4, 4);
                g.setPaint(new LinearGradientPaint(
                        bounds.x, bounds.y, bounds.x + bounds.width, bounds.y,
                        WHITE_KEY_GRADIENT_DISTRIBUTIONS,
                        COLOR_WHITE_KEY_GRADIENT
                ));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 4, 4);

//                final int midiNote = Note.fromPianoKeyIndexToMidiNote(i);
                int moveAmt = (i > 10) ? 6 : 12;
                g.setColor(Color.RED);
                g.drawString(Integer.toString(i), bounds.x + 0, bounds.y + bounds.height - 20);
//
//                NoteDetails noteDetails = NoteDetails.from(i);
//                g.setColor(Color.YELLOW.darker());
//                g.drawString(noteDetails.getNoteName(), bounds.x + 0, bounds.y + bounds.height - 40);
//
//                g.setColor(Color.GREEN);
//                g.drawString(Integer.toString(midiNote), bounds.x + 0, bounds.y + bounds.height - 60);


            }
            else {
                g.setPaint(new LinearGradientPaint(
                        bounds.x, bounds.y, bounds.x, bounds.y + bounds.height,
                        BLACK_KEY_GRADIENT_DISTRIBUTIONS,
                        COLOR_BLACK_KEY_GRADIENT
                ));
                g.fill(ks.getShape());

                //Draw red text on white keys
                int moveAmt = (i > 10) ? 6 : 12;
                g.setColor(Color.RED);
                g.drawString(Integer.toString(i), bounds.x + moveAmt, bounds.y + bounds.height - 20);

                int midiNote = (i + 21);
                //NoteDetails noteDetails = NoteDetails.from(i);
                //g.setColor(Color.YELLOW.darker());
                // g.drawString(noteDetails.getNoteName(), bounds.x + moveAmt, bounds.y + bounds.height - 40);

                //g.setColor(Color.GREEN);
                //g.drawString(Integer.toString(midiNote), bounds.x + moveAmt, bounds.y + bounds.height - 60);

                if(midiNote == 60) {
                    g.setColor(Color.RED);
                    g.drawString("***", bounds.x + moveAmt, bounds.y + bounds.height - 80);
                }
            }

            g.setColor(Color.BLACK);
            g.draw(ks.getShape());


        }
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
                WHITE_KEY_COUNT * WHITE_KEY_WIDTH,
                WHITE_KEY_HEIGHT
        );
    }


    public int getKeyAtPoint(Point2D p) {
        List<KeyShape> keyShapes = getKeyShapes();
        for (int i = 0; i < keyShapes.size(); i++) {
            if (keyShapes.get(i).getShape().contains(p)) return i;
        }
        return -1;
    }

    public void setKeyLit(NoteEvent note, Color color) {
        setKeyIndexLit(note.getKeyNumber() - 21, note.isNoteOn(), color);
    }

    @Deprecated
    public void setKeyIndexLit(int index, boolean lit, Color color) {
        if (index < 0 || index > getKeyShapes().size()) return;
        if (lit) {
            litKeys.put(index, color);
        }
        else {
            litKeys.remove(index);
        }
        repaint(getKeyShapes().get(index).getShape().getBounds());
    }

    public boolean isKeyLit(int index) {
        return litKeys.containsKey(index);
    }


    public void clearLitKeys() {
        litKeys.clear();
        repaint();
    }
}
