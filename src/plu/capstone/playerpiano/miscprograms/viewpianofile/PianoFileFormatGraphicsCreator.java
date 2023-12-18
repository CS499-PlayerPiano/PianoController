package plu.capstone.playerpiano.miscprograms.viewpianofile;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics.NameAndByte;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParser;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

public class PianoFileFormatGraphicsCreator {

    private Queue<BufferedPianoFileReaderForGraphics.NameAndByte> nameAndBytes;

    public PianoFileFormatGraphicsCreator(BufferedPianoFileReaderForGraphics reader) {
        this.nameAndBytes = reader.getNameAndBytes();
    }

    public static void main(String[] args) throws Exception {
        BufferedPianoFileReaderForGraphics reader = new BufferedPianoFileReaderForGraphics(new File("tmp/v6.piano"));

        reader.readShort(SheetMusicFileParser.VERSION);
        SheetMusicReaderWriter.V6.getFileParser().readSheetMusic(reader);
        reader.close();

        PianoFileFormatGraphicsCreator creator = new PianoFileFormatGraphicsCreator(reader);
        creator.show();
    }

    private JPanel picturePanel;

    public void show() {

        JFrame frame = new JFrame("Piano File Format Graphics");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(1000, 1000);

        NameAndByte[] arrayFromQueue = convertQueueToArray(nameAndBytes);

        //Remove duplicate rows
        List<NameAndByte> deduplicatedList = new ArrayList<>();
        for(NameAndByte object : arrayFromQueue) {
            if(!deduplicatedList.contains(object)) {
                deduplicatedList.add(object);
            }
        }

        picturePanel = new JPanel(new BorderLayout());
        picturePanel.add( new TablePanel(arrayFromQueue).get());
        picturePanel.add(new KeyPanel(deduplicatedList), BorderLayout.EAST);

        JPanel controlsPanel = new JPanel();

        JButton screenshotButton = new JButton("Screenshot");
        screenshotButton.addActionListener(e -> {
            screenshot();
        });

        controlsPanel.add(screenshotButton);

        frame.add(controlsPanel, BorderLayout.NORTH);
        frame.add(picturePanel);
        frame.pack();
        frame.setVisible(true);
    }

    private void screenshot() {

        BufferedImage image = new BufferedImage(picturePanel.getWidth(), picturePanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        picturePanel.paint(image.getGraphics());
        try {
            ImageIO.write(image, "png", new File("tmp/screenshot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //convert queue of Object into array of Object
    private NameAndByte[] convertQueueToArray(Queue<NameAndByte> queue) {
        NameAndByte[] array = new NameAndByte[queue.size()];
        int index = 0;
        for(NameAndByte object : queue) {
            array[index] = object;
            index++;
        }
        return array;
    }

}
