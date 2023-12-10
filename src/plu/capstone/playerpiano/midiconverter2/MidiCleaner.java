package plu.capstone.playerpiano.midiconverter2;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
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

public class MidiCleaner {

    private static final Logger LOGGER = new Logger(MidiCleaner.class);

    private static final MidiConversionStep[] steps = {
            new Step1RemoveNonPianoKeys(),
            new Step2RemoveInvalidChannels(),
            new Step3OnNoteVelocity0Fix(),
            new Step4MaxOnNotesAtATime(45),
            new Step5InsertingOffNotes(200),
    };

    private static final File MIDI_FILE = new File("res/songs/Testing/RUSH_E_FINAL.mid");

    public static void main(String[] args) throws InvalidMidiDataException, IOException {

        TimingsReport timingsReport = new TimingsReport();

        Timing createSheetMusic = timingsReport.newTiming("Read midi file");
        SheetMusic sheetMusic = new MidiSheetMusic(MIDI_FILE);
        createSheetMusic.stop();

        for(int i = 0; i < steps.length; i++) {
            Timing timing = timingsReport.newTiming("Step " + (i + 1) + ": " + steps[i].getName());
            final MidiConversionStep step = steps[i];
            LOGGER.info("Running step: " + step.getName());
            step.process(sheetMusic);
            timing.stop();
        }

        //Write the file

        Timing writeSheetMusic = timingsReport.newTiming("Write File");
        SheetMusicReaderWriter.saveSheetMusic(sheetMusic, new File("tmp/RushECleaned.piano"), SheetMusicReaderWriter.LATEST_VERSION);
        writeSheetMusic.stop();

        timingsReport.printReport();

    }

}
