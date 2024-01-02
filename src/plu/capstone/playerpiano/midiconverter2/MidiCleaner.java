package plu.capstone.playerpiano.midiconverter2;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.controller.utilities.timings.Timing;
import plu.capstone.playerpiano.controller.utilities.timings.TimingsReport;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.midiconverter2.steps.Step1RemoveNonPianoKeys;
import plu.capstone.playerpiano.midiconverter2.steps.Step2RemoveInvalidChannels;
import plu.capstone.playerpiano.midiconverter2.steps.Step3OnNoteVelocity0Fix;
import plu.capstone.playerpiano.midiconverter2.steps.Step4MaxOnNotesAtATime;
import plu.capstone.playerpiano.midiconverter2.steps.Step5InsertingOffNotes;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;

@AllArgsConstructor
public class MidiCleaner implements Runnable{

    private static final Logger LOGGER = new Logger(MidiCleaner.class);

    private static final MidiConversionStep[] steps = {
            new Step1RemoveNonPianoKeys(),
            new Step2RemoveInvalidChannels(),
            new Step3OnNoteVelocity0Fix(),
            new Step4MaxOnNotesAtATime(45),
            new Step5InsertingOffNotes(200),
    };

    private final File INPUT_FILE;
    private final File OUTPUT_FILE;
    private final int VERSION;

    @Override
    public void run() {

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
