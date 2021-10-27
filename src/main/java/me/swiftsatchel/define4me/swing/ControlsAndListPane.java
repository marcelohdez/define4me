package me.swiftsatchel.define4me.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel which contains a JList and "controls" in a flow layout under the JList
 */
public class ControlsAndListPane extends JPanel {

    public ControlsAndListPane(JList<String> list, JButton... controls) {

        setLayout(new BorderLayout());
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(createButtonRow(controls), BorderLayout.SOUTH);

    }

    private JPanel createButtonRow(JButton... buttons) {

        JPanel pnl = new JPanel();

        for (JButton b : buttons)
            pnl.add(b);

        return pnl;

    }

}
