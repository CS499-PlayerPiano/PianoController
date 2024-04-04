package plu.capstone.playerpiano.outputs.statisticsdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.outputs.Output;
import plu.capstone.playerpiano.sheetmusic.events.NoteEvent;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.sheetmusic.events.SustainPedalEvent;

public class OutputStatisticsDB extends Output {

    //TODO: Make settings file
    private static final long SAVE_INTERVAL = 1000 * 60 * 1; // 1 MINUTE
    private static final String JDBC_URL = "jdbc:sqlite:res/statistics.db";
    private static final int QUERY_TIMEOUT = 30; //seconds
    private static final int SEND_TO_WEBSITE_INTERVAL = 100; //milliseconds

    private Statement statement;
    private RawStatistics statistics = new RawStatistics();

    private Logger logger = new Logger(this);

    //Access by webserver
    public int getSendToWebsiteInterval() {return SEND_TO_WEBSITE_INTERVAL;}

    @Override
    protected void onEnable() {

        try {
            connectToDB();
            statistics.read(statement);
        }
        catch(SQLException e) {
            logger.error("Error connecting to the database", e);
            return;
        }

        //Save the statistics to the DB every SAVE_INTERVAL
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    statistics.write(statement);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, SAVE_INTERVAL, SAVE_INTERVAL);
    }

    private void connectToDB() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL);
        statement = connection.createStatement();
        statement.setQueryTimeout(QUERY_TIMEOUT);
    }

    @Override
    public String getName() {
        return "Statistics-DB";
    }

    @Override
    public void onSustainPedal(SustainPedalEvent event, long timestamp) {
        if(event.isOn()) {
            statistics.incrementTotalSustainPedalPressed();
        }
    }

    //TODO: Is this the best way to handle this?
    @Override
    public void onTimestampEvent(long current, long end) {
        statistics.incrementMillisecondsPlayed(end - current);
    }

    @Override
    public void onSongStarted(long timestamp, Map<Long, List<SheetMusicEvent>> entireNoteMap) {
        statistics.incrementTotalSongsPlayed();
    }

    @Override
    public void onNotePlayed(NoteEvent note, long timestamp) {
        if(note.isNoteOn()) {
            statistics.incrementTotalNotesPlayed();
        }
    }
}
