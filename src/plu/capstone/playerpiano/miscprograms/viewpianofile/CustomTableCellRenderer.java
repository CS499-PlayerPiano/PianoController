package plu.capstone.playerpiano.miscprograms.viewpianofile;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics.NameAndByte;

public class CustomTableCellRenderer extends DefaultTableCellRenderer  {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value instanceof NameAndByte) {
            NameAndByte val = (NameAndByte) value;

            JLabel label = new JLabel(val.getValueAsString());
            label.setOpaque(true);
            label.setBackground(val.getColor());
            label.setToolTipText(val.getName());

            cellComponent = label;
        }

        //Center all text
        if(cellComponent instanceof JLabel) {
            JLabel label = (JLabel) cellComponent;

            //Center the text
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }

        return cellComponent;
    }

}
