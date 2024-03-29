package plu.capstone.playerpiano.subprogram.midiviewer;

import java.io.File;
import picocli.CommandLine.Option;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.cleaner.MidiCleanerSM;
import plu.capstone.playerpiano.subprogram.SubProgram;

public class SubProgramMidiViewer extends SubProgram {

    @Option(
            names = {"-f", "--file"},
            description = "The midi file to view",
            required = true
    )
    private File file;

    @Option(
            names = {"--showCleanedOnly"},
            description = "Show cleaned file only?",
            defaultValue = "false"
    )
    private boolean cleanedOnly = false;

    @Option(
            names = {"--showOrigAndCleaned"},
            description = "Show original and cleaned files?",
            defaultValue = "false"
    )
    private boolean showOrigAndCleaned = false;

    @Override
    public String getSubCommand() {
        return "midi-viewer";
    }

    @Override
    public void run() throws Exception {

        SheetMusic sm = new MidiSheetMusic(file);
        String title = "Original";

        if(showOrigAndCleaned && !cleanedOnly) {
            SheetMusic cloned = (SheetMusic)sm.clone();
            new SheetMusicViewer(cloned.getEventMap()).createAndShowWindow("Original");
        }

        if(cleanedOnly) {
            sm = MidiCleanerSM.applyChanges(sm);
            title = "Cleaned";
        }

        new SheetMusicViewer(sm.getEventMap()).createAndShowWindow(title);

    }
}
