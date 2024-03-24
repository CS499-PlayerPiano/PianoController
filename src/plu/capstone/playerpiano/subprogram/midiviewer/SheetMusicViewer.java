package plu.capstone.playerpiano.subprogram.midiviewer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import plu.capstone.playerpiano.logger.Logger;
import plu.capstone.playerpiano.sheetmusic.events.SheetMusicEvent;
import plu.capstone.playerpiano.utilities.graphics.RowNumberTable;
import plu.capstone.playerpiano.sheetmusic.events.Note;

public class SheetMusicViewer {

    private final Logger logger = new Logger(this);


    private Map<Long, List<Note>> newNoteMap = new HashMap<>();

    Integer[][] data;
    String[] columnNames;
    int maxChannel;

    public SheetMusicViewer(Map<Long, List<SheetMusicEvent>> noteMap) {
        //this.noteMap = noteMap;

        maxChannel = 0;

        for(Long time : noteMap.keySet()) {
            for(SheetMusicEvent event : noteMap.get(time)) {
                if(event instanceof Note) {
                    Note note = (Note)event;
                    if (note.getChannelNum() > maxChannel) {
                        maxChannel = note.getChannelNum();
                    }
                }
            }
        }

        for(Long time : noteMap.keySet()) {

            newNoteMap.put(time, new ArrayList<>());

            for(int i = 0; i <= maxChannel; i++) {
                newNoteMap.get(time).add(null);
            }

            for(SheetMusicEvent event : noteMap.get(time)) {
                if(!(event instanceof Note)) {
                    continue;
                }

                Note note = (Note)event;
                int row = note.getChannelNum();

                newNoteMap.get(time).add(row, note);

            }
        }

       logger.info("newNoteMap: " + newNoteMap.size());


        //covert notemap to 2d string array
        columnNames = new String[newNoteMap.size()];

        for(int i = 0; i < newNoteMap.size(); i++) {
            columnNames[i] = "" + newNoteMap.keySet().toArray()[i];
        }

        //ROW, COL
        data = new Integer[maxChannel + 1][newNoteMap.size()];

        for(int i = 0; i < data.length; i++) {
            data[i] = new Integer[newNoteMap.size()];

            for(int j = 0; j < data[i].length; j++) {
                data[i][j] = 0;
            }
        }

        for(int i = 0; i < newNoteMap.size(); i++) {
            long time = (long)newNoteMap.keySet().toArray()[i];
            for(Note note : newNoteMap.get(time)) {
                if(note != null) {
                    data[note.getChannelNum()][i] = note.getKeyNumber();
                }
            }
        }

    }

    public void createAndShowWindow(String windowName) {

        for(int i = 0; i < columnNames.length; i++) {
            columnNames[i] = "";
        }

        JTable table = new JTable(data, columnNames){
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return new CustomCellRenderer();
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

       logger.info("datalen: " + data.length);
       logger.info("data[0]len: " + data[0].length);
       logger.info("columnNameslen: " + columnNames.length);

        JFrame frame = new JFrame("SMView - " + windowName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        resizeColumnWidth(table);

        JScrollPane scrollPane = new JScrollPane(table);

        List<String> rowNames = new ArrayList<>();
        for(int i = 0; i < maxChannel; i++) {
            rowNames.add("" + i);
        }

        JTable rowTable = new RowNumberTable(table, rowNames);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
                rowTable.getTableHeader());

        frame.add(scrollPane);


        frame.setVisible(true);
    }

    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            if(width > 300)
                width=300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

}
