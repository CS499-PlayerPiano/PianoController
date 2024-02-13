package plu.capstone.playerpiano.miscprograms.viewpianofile;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics.NameAndByte;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicFileParser;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

@AllArgsConstructor
public class PianoFileFormatGraphicsCreator implements Runnable {

    private final File inputFile;
    private final File outputFile;

    @Override
    public void run() {

        sanityCheckFiles();

        Queue<BufferedPianoFileReaderForGraphics.NameAndByte> nameAndBytes;

        try {
            BufferedPianoFileReaderForGraphics reader = new BufferedPianoFileReaderForGraphics(inputFile);

            short v = reader.readShort(SheetMusicFileParser.VERSION);
            SheetMusicReaderWriter.getByVersion(v).readSheetMusic(reader);
            reader.close();

            nameAndBytes = reader.getNameAndBytes();
            show(nameAndBytes);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void sanityCheckFiles() {
        if(!inputFile.exists()) {
            throw new IllegalArgumentException("Input file does not exist");
        }

        if(!inputFile.isFile()) {
            throw new IllegalArgumentException("Input file is not a file");
        }

        if(!inputFile.canRead()) {
            throw new IllegalArgumentException("Input file cannot be read");
        }

        //check file extension
        if(!inputFile.getName().endsWith(".piano")) {
            throw new IllegalArgumentException("Input file must have a .piano extension");
        }

        if(outputFile.exists()) {
            throw new IllegalArgumentException("Output file already exists");
        }

        //check file extension
        if(!outputFile.getName().endsWith(".png")) {
            throw new IllegalArgumentException("Output file must have a .png extension");
        }
    }



    private void show(Queue<BufferedPianoFileReaderForGraphics.NameAndByte> nameAndBytes) {

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

        JPanel picturePanel = new JPanel(new BorderLayout());
        picturePanel.add( new TablePanel(arrayFromQueue).get());
        picturePanel.add(new KeyPanel(deduplicatedList), BorderLayout.EAST);

        JPanel controlsPanel = new JPanel();

        JButton screenshotButton = new JButton("Screenshot");
        screenshotButton.addActionListener(e -> {
            screenshot(picturePanel);
        });

        controlsPanel.add(screenshotButton);

        frame.add(controlsPanel, BorderLayout.NORTH);
        frame.add(picturePanel);
        frame.pack();
        frame.setVisible(true);
    }

    private void screenshot(JPanel picturePanel) {

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
