package plu.capstone.playerpiano.outputs.statisticsdb;

import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.plaf.nimbus.State;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plu.capstone.playerpiano.sheetmusic.SheetMusicCallback;

@NoArgsConstructor
@Getter
public class RawStatistics {

    private long millisecondsPlayed;
    private long totalSongsPlayed;
    private long totalNotesPlayed;
    private long totalSustainPedalPressed;

    public void incrementTotalNotesPlayed() {totalNotesPlayed ++;}
    public void incrementTotalSongsPlayed() {totalSongsPlayed ++;}
    public void incrementTotalSustainPedalPressed() {totalSustainPedalPressed ++;}
    public void incrementMillisecondsPlayed(long milliseconds) {millisecondsPlayed += milliseconds;}

    public void read(Statement statement) throws SQLException {

    }

    public void write(Statement statement) throws SQLException {

    }

}
