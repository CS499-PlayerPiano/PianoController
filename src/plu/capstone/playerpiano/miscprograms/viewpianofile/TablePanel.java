package plu.capstone.playerpiano.miscprograms.viewpianofile;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import plu.capstone.playerpiano.utilities.graphics.RowNumberTable;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics.NameAndByte;

public class TablePanel {

    private static final String[] COLUMNS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    int widthHeight = 30;

    private JScrollPane scrollPane;

    public TablePanel(NameAndByte[] arrayFromQueue) {

        Object[][] data = turnObjectArrayIntoObjectArrayWithColumns(arrayFromQueue, 16);

        JTable table = new JTable(data, COLUMNS){
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return new CustomTableCellRenderer();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public boolean isCellSelected(int row, int column) {
                return false;
            }


        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        table.setRowHeight(30);
        for(int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widthHeight);
        }

        table.setGridColor(Color.BLACK);
        table.setPreferredScrollableViewportSize(new Dimension(480, 150));

        scrollPane = new JScrollPane(table);

        List<String> rowNames = new ArrayList<>();
        for(int i = 0; i < data.length; i++) {
            rowNames.add(generateRowName(i));
        }

        JTable rowTable = new RowNumberTable(table, rowNames);
        scrollPane.setRowHeaderView(rowTable);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
                rowTable.getTableHeader());

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    }

    public final JScrollPane get() {
        return scrollPane;
    }

    private static String generateRowName(int index) {

        String rowName = "" + (index * 10);
        while(rowName.length() < 5) {
            rowName = "0" + rowName;
        }
        return rowName;
    }

    private static Object[][] turnObjectArrayIntoObjectArrayWithColumns(Object[] array, int columns) {
        int rows = (int) Math.ceil((double) array.length / columns);
        Object[][] newArray = new Object[rows][columns];
        int index = 0;
        for (int i = 0; i < newArray.length; i++) {
            for (int j = 0; j < newArray[i].length; j++) {
                if (index < array.length) {
                    newArray[i][j] = array[index];
                    index++;
                }
            }
        }
        return newArray;
    }
}
