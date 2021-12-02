package me.swiftsatchel.define4me.swing.dialog;

import me.swiftsatchel.define4me.util.Settings;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * JDialog with preferences to certain aspects of the program
 */
public class PrefsDialog extends JDialog implements WindowListener {

    // Definition preference
    private final JRadioButton preferFirstDefinition = new JRadioButton("First");
    private final JRadioButton preferToAskDefinition = new JRadioButton("Ask If Multiple");
    // Mac menu bar preference
    private final JRadioButton preferMacMenuBar = new JRadioButton("macOS");
    private final JRadioButton preferInAppMenuBar = new JRadioButton("In-App");

    public PrefsDialog() {

        setTitle("Preferences");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        addWindowListener(this);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        initComps();
        createRadioRow("Definition choice:", preferFirstDefinition, preferToAskDefinition);
        if (System.getProperty("os.name").equals("Mac OS X"))
            createRadioRow("Menu bar style (requires restart):", preferMacMenuBar, preferInAppMenuBar);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void initComps() {

        // Definition choice
        preferFirstDefinition.setSelected(Settings.prefersFirstDefinition());
        preferToAskDefinition.setSelected(!preferFirstDefinition.isSelected());
        // Mac menu bar choice
        preferMacMenuBar.setSelected(Settings.prefersMacMenuBar());
        preferInAppMenuBar.setSelected(!preferMacMenuBar.isSelected());

    }

    private void createRadioRow(String labelText, JRadioButton... buttons) {

        JPanel pnl = new JPanel();
        ButtonGroup group = new ButtonGroup(); // Only one radio button can be activated per group

        pnl.add(new JLabel(labelText));
        for (JRadioButton button : buttons) {
            group.add(button);
            pnl.add(button);
        }

        add(pnl);

    }

    @Override
    public void windowClosing(WindowEvent e) {

        Settings.setDefinitionPreference(preferFirstDefinition.isSelected());
        Settings.setMacMenuBarPreference(preferMacMenuBar.isSelected());

    }

    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosed(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}

}
