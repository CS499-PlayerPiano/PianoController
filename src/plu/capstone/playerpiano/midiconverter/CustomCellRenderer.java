package plu.capstone.playerpiano.midiconverter;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CustomCellRenderer extends DefaultTableCellRenderer {

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        boolean render = (int) value != 0;

        Component rendererComp = super.getTableCellRendererComponent(table, render ? value : "", isSelected, hasFocus, row, column);

        if(value instanceof Integer) {
            int valueInt = (int) value;

            if(valueInt == 0) {
                rendererComp.setBackground(Color.BLACK);
            }
            else {
                Color color = new Color(Color.HSBtoRGB(valueInt / 127f, 0.7f, 1.0f));
                rendererComp.setBackground(color);
            }
        }

        //System.out.println("value: " + value.getClass().getSimpleName());

        return rendererComp;
    }

    public Color getHuedColor(int hue) {
        return new Color(
                Color.HSBtoRGB(85 / 360f * hue, 0.7f, 1.0f
                ));
    }
}
