package plu.capstone.playerpiano.outputs.logger;

import java.util.List;
import java.util.Map;
import plu.capstone.playerpiano.logger.ConsoleColors;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.sheetmusic.events.Note;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;

/**
 * Logs all notes played to the console.
 */
public class OutputLogger extends Output {

    @Override
    public String getName() {
        return "Logger";
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        super.onSongStarted(timestamp, entireNoteMap);
        logger.info("Song Started");
    }

    @Override
    public void onSongFinished(long timestamp) {
        super.onSongFinished(timestamp);
        logger.info("Song Finished");
    }

    @Override
    public void onPause() {
        super.onPause();
        logger.info("Song Paused");
    }

    @Override
    public void onUnpause() {
        super.onUnpause();
        logger.info("Song Unpaused");
    }

    @Override
    public void onTimestampEvent(long current, long end) {
        super.onTimestampEvent(current, end);
        logger.info("Current Time: " + current + "ms, End Time: " + end + "ms");
    }

    @Override
    public void onSustainPedal(SustainPedalEvent event, long timestamp) {
        super.onSustainPedal(event, timestamp);
        logger.info("Sustain Pedal " + (event.isOn() ? "ON" : "OFF"));
    }

    @Override
    public void onNotesPlayed(List<Note> notes, long timestamp) {

        StringBuilder sb = new StringBuilder();
        sb.append("Notes Played: [");
        for(int i = 0; i < notes.size(); i++) {

            Note note = notes.get(i);
            sb.append("(N: ");
            sb.append(note.getKeyNumber());
            sb.append(", O: ");
            sb.append(note.isNoteOn());
            sb.append(", V: ");
            sb.append(note.getVelocity());
            sb.append(")");

            if(i != notes.size() - 1)
                sb.append(", ");

            sb.append("]");
        }

        logger.info(sb.toString());

    }
}
