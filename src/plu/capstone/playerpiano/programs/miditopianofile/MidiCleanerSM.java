package plu.capstone.playerpiano.programs.miditopianofile;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import lombok.AllArgsConstructor;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step1RemoveNonPianoKeys;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step2RemoveInvalidChannels;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step3OnNoteVelocity0Fix;
import plu.capstone.playerpiano.programs.miditopianofile.steps.Step4OffsetNoteTimes;
import plu.capstone.playerpiano.sheetmusic.MidiSheetMusic;
import plu.capstone.playerpiano.sheetmusic.SheetMusic;
import plu.capstone.playerpiano.sheetmusic.serializable.SheetMusicReaderWriter;
import plu.capstone.playerpiano.utilities.timings.TimingsReport;

public class MidiCleanerSM {

    private static final Logger LOGGER = new Logger(MidiCleanerSM.class);

    public static final SheetMusic applyChanges(SheetMusic sheetMusic) {

        MidiConversionStep[] steps = {
                new Step1RemoveNonPianoKeys(),
                new Step2RemoveInvalidChannels(),
                new Step3OnNoteVelocity0Fix(),
                new Step4OffsetNoteTimes()
        };


        TimingsReport timingsReport = new TimingsReport();

        for (int i = 0; i < steps.length; i++) {
            timingsReport.start("Step " + (i + 1) + ": " + steps[i].getName());
            final MidiConversionStep step = steps[i];
            LOGGER.info("Running step: " + step.getName());
            step.process(sheetMusic);
            timingsReport.stop();
        }

        timingsReport.printReport();

        return sheetMusic;
    }


}
