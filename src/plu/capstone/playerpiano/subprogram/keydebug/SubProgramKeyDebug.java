package plu.capstone.playerpiano.subprogram.keydebug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.subprogram.SubProgram;
import plu.capstone.playerpiano.utilities.graphics.JTextNumericField;
import plu.capstone.playerpiano.utilities.graphics.piano.ComponentPiano;

/**
 * Plugin to
 */
public class SubProgramKeyDebug extends SubProgram {

    private final JFrame frame = new JFrame("PianoPattern");
    private final ComponentPiano piano = new ComponentPiano();

    private int theKey;
    private boolean started = false;

    private final Logger logger = new Logger(this);

    JTextNumericField onTimeInput;
    JTextNumericField offTimeInput;
    JTextNumericField velocityInput;

    @Override
    public String getSubCommand() {
        return "key-debug";
    }

    @Override
    public void run() throws Exception {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        piano.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                // Left mouse click
                if(e.getButton() == MouseEvent.BUTTON1) {
                    theKey = piano.getKeyAtPoint(e.getPoint());

                    Note[] toSend = new Note[88];

                    for(int i = 0; i < 88; i++) {

                        Note tmpNote = new Note(Note.fromPianoKeyIndexToMidiNote(i), (byte) 0, false);
                        piano.setKeyLit(tmpNote, null);
                        toSend[i] = tmpNote;
                    }


                    if(theKey != -1) {
                        Note tmpNote = new Note(Note.fromPianoKeyIndexToMidiNote(theKey), Note.MAX_VELOCITY, true);
                        piano.setKeyLit(tmpNote, Color.YELLOW);
                        toSend[theKey] = tmpNote;
                    }

                    //playNotes(Arrays.stream(toSend).toList());

                }

            }
        });

        piano.setBackgroundColor(Color.GREEN.darker().darker());

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(piano), BorderLayout.CENTER);


        // Panel for buttons, dropdown, and number inputs
        JPanel controlsPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        // Start and stop buttons
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            if(!started) {
                this.started = true;
                new Thread(() -> {

                    while(this.started) {

                        //only hit the key once
//                        if(!looping) {
//                            this.started = false;
//                        }

                        try {
                            playNote(new Note(Note.fromPianoKeyIndexToMidiNote(theKey), (byte) velocityInput.getValue(), true));
                            Thread.sleep(onTimeInput.getValue());
                            playNote(new Note(Note.fromPianoKeyIndexToMidiNote(theKey), (byte) 0, false));
                            Thread.sleep(offTimeInput.getValue());
                        }
                        catch(Exception igored) {}

                    }

                }, "PatternDebug Send Thread").start();
            }
            else {
                logger.warning("Failed to start, already started!");
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            this.started = false;
        });

        controlsPanel.add(startButton);
        controlsPanel.add(stopButton);

        // Dropdown
        String[] options = {"Looping", "Single Hit"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        controlsPanel.add(new JLabel("(Not implemented) Looping Type:"));
        controlsPanel.add(dropdown);

        // OnTime and OffTime inputs
        onTimeInput = new JTextNumericField(0, Integer.MAX_VALUE, 50);
        offTimeInput = new JTextNumericField(0, Integer.MAX_VALUE, 50);
        velocityInput = new JTextNumericField(0, Note.MAX_VELOCITY, Note.MAX_VELOCITY);

        controlsPanel.add(new JLabel("OnTime:"));
        controlsPanel.add(onTimeInput);
        controlsPanel.add(new JLabel("OffTime:"));
        controlsPanel.add(offTimeInput);
        controlsPanel.add(new JLabel("Velocity:"));
        controlsPanel.add(velocityInput);

        frame.add(controlsPanel, BorderLayout.SOUTH);


        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
        });
    }

}
