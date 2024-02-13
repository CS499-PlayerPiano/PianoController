package plu.capstone.playerpiano.programs.miditopianofile;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.utilities.timings.TimingsReport;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step1RemoveNonPianoKeys;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step2RemoveInvalidChannels;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step3OnNoteVelocity0Fix;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step4MaxOnNotesAtATime;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step5InsertingOffNotes;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

@AllArgsConstructor
public class MidiCleaner implements Runnable{

    private static final Logger LOGGER = new Logger(MidiCleaner.class);



    private final File INPUT_FILE;
    private final File OUTPUT_FILE;
    private final int VERSION;
    private final int MAX_NOTES_ON_AT_A_TIME;
    private final long DUPLICATE_ON_LAG_TIME;



    @Override
    public void run() {

        MidiConversionStep[] steps = {
                new Step1RemoveNonPianoKeys(),
                new Step2RemoveInvalidChannels(),
                new Step3OnNoteVelocity0Fix(),
                new Step4MaxOnNotesAtATime(MAX_NOTES_ON_AT_A_TIME),
                new Step5InsertingOffNotes(DUPLICATE_ON_LAG_TIME),
        };

        try {

            TimingsReport timingsReport = new TimingsReport();

            timingsReport.start("Read midi file");
            SheetMusic sheetMusic = new MidiSheetMusic(INPUT_FILE);
            timingsReport.stop();

            for (int i = 0; i < steps.length; i++) {
                timingsReport.start("Step " + (i + 1) + ": " + steps[i].getName());
                final MidiConversionStep step = steps[i];
                LOGGER.info("Running step: " + step.getName());
                step.process(sheetMusic);
                timingsReport.stop();
            }

            //Write the file

            timingsReport.start("Write File");
            SheetMusicReaderWriter.saveSheetMusic(sheetMusic, OUTPUT_FILE, VERSION);
            timingsReport.stop();

            timingsReport.printReport();
        }
        catch (IOException | InvalidMidiDataException e) {
            e.printStackTrace();
        }

    }

}
