package plu.capstone.playerpiano.miscprograms.viewpianofile;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import plu.capstone.playerpiano.sheetmusic.io.BufferedPianoFileReaderForGraphics.NameAndByte;

public class KeyPanel extends JPanel {

    public KeyPanel(List<NameAndByte> colorItems) {

        JPanel colorKeysPanel = new JPanel();
        colorKeysPanel.setLayout(new GridLayout(colorItems.size(), 1));
        colorKeysPanel.setMaximumSize( colorKeysPanel.getPreferredSize() );

        for (NameAndByte item : colorItems) {
            JPanel colorItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(20, 20)); // Setting the size of the color box
            colorBox.setBackground(item.getColor());

            JLabel colorLabel = new JLabel(item.getName());

            colorItemPanel.add(colorBox);
            colorItemPanel.add(colorLabel);

            colorKeysPanel.add(colorItemPanel);
        }

        this.add(colorKeysPanel);
    }

}
