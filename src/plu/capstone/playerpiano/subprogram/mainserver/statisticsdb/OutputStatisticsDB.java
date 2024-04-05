package plu.capstone.playerpiano.subprogram.mainserver.statisticsdb;

import java.util.Timer;
import java.util.TimerTask;
import lombok.Getter;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;
import plu.capstone.playerpiano.subprogram.mainserver.SubProgramMainController;

public class OutputStatisticsDB extends Output {

    private Logger logger = new Logger(this);

    private final SubProgramMainController controller;
    public OutputStatisticsDB(SubProgramMainController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Statistics-DB";
    }

    @Override
    public void onSustainPedal(SustainPedalEvent event, long timestamp) {
        if(event.isOn()) {
            controller.getStatistics().incrementTotalSustainPedalPressed();
        }
    }

    //TODO: Is this the best way to handle this?
    @Override
    public void onTimestampEvent(long current, long end) {
        //controller.getStatistics().incrementMillisecondsPlayed(current);
    }

    //Now in QueueManager
//    @Override
//    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
//        statistics.incrementTotalSongsPlayed();
//    }

    @Override
    public void onNotePlayed(NoteEvent note, long timestamp) {
        if(note.isNoteOn()) {
            controller.getStatistics().incrementTotalNotesPlayed();
        }
    }

}
