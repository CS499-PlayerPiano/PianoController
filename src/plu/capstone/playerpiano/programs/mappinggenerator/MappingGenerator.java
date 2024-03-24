package plu.capstone.playerpiano.programs.mappinggenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.plugins.PluginInstances;
import plu.capstone.playerpiano.plugins.impl.PluginArduinoPiano.PluginRealPiano;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.utilities.graphics.piano.ComponentPiano;

public class MappingGenerator {

    static final JFrame frame = new JFrame("PianoPattern");
    static final ComponentPiano piano = new ComponentPiano();

    static int hasPickedKey = -1;

    static int currentKeyWeAreMapping = 0;

    static PluginRealPiano realPiano;

    static Set<Integer> alreadyConfigured = new HashSet<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Config generator for the Arduino Piano Plugin");

        File file = new File("tmp/cfg-physicalPianoMapping.json");
        if(file.exists()) {
            System.out.println("tmp/cfg-physicalPianoMapping.json already exists! Please delete it manually to ensure you want to overwrite it.");
            return;
        }

        //Hack to load the plugins without running the main controller. Really need to redo this code.
        //realPiano = (PluginRealPiano) new PluginLoader().loadPlugin(PluginInstances.PHYSICAL_PIANO.getPluginClass());
        //TODO: FIX

        realPiano.loadPlugin();

        piano.setBackgroundColor(Color.YELLOW);
        frame.add(new JScrollPane(piano));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });



        piano.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                // Left mouse click
                if(e.getButton() == MouseEvent.BUTTON1) {
                    int theKey = piano.getKeyAtPoint(e.getPoint());

                    turnOffAllKeys();

                    if(theKey != -1) {
                        piano.setKeyIndexLit(theKey, true, Color.GREEN);
                    }

                    hasPickedKey = theKey;

                }

            }
        });

        new Thread(() -> {
            try {
                while(true) {
                    if (hasPickedKey != -1) {
                        Thread.sleep(10);
                        continue;
                    }

                    synchronized (realPiano) {
                        realPiano.sendRawIndexWithoutMapping(currentKeyWeAreMapping, true, (byte) 255);
                    }
                    Thread.sleep(500);
                    synchronized (realPiano) {
                        realPiano.sendRawIndexWithoutMapping(currentKeyWeAreMapping, false, (byte) 0);
                    }

                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


        for(int physicalKey = 0; physicalKey < 88; physicalKey++) {


            System.out.println("Please prress what key you hear / see on the physical piano");
            currentKeyWeAreMapping = physicalKey;
            while(hasPickedKey == -1) {
                Thread.sleep(100);
            }
            final int tmpKey = hasPickedKey;
            hasPickedKey = -1;

            piano.setKeyIndexLit(tmpKey, false, null);

            int midiNote = Note.fromPianoKeyIndexToMidiNote(tmpKey);

            addMapping(physicalKey, midiNote);
            alreadyConfigured.add(tmpKey);
            updateUsedKeys();
            System.out.println("Physical Key " + physicalKey + " is mapped to midi note" + tmpKey);
        }

        System.out.println("Done mapping keys!");

    }

    static JsonObject mappings = new JsonObject();
    static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static void addMapping(int physicalKey, int midiNote) {

        mappings.addProperty(Integer.toString(physicalKey), midiNote);

        String json = GSON.toJson(mappings);
        File file = new File("tmp/cfg-physicalPianoMapping.json");
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void turnOffAllKeys() {
        for(int i = 0; i < 88; i++) {
            piano.setKeyIndexLit(i, false, null);
        }
        synchronized (realPiano) {
            realPiano.onSongFinished(0);
        }
        updateUsedKeys();
    }

    private static void updateUsedKeys() {
        for(int i = 0; i < 88; i++) {
            if(alreadyConfigured.contains(i)) {
                piano.setKeyIndexLit(i, true, Color.BLUE);
            }
        }
    }

}
