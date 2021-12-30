package me.soggysandwich.define4me.swing.dialog;

import me.soggysandwich.define4me.Define4Me;
import me.soggysandwich.define4me.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * JDialog with preferences to certain aspects of the program
 */
public class PrefsDialog extends JDialog implements WindowListener {

    // Definition preference
    private final JRadioButton preferFirstDefinition = new JRadioButton("First");
    private final JRadioButton preferToAskDefinition = new JRadioButton("Ask If Multiple");
    // Wikipedia summary preference
    private final JCheckBox useWikipedia = new JCheckBox("Use Wikipedia");
    // Mac menu bar preference
    private final JRadioButton preferMacMenuBar = new JRadioButton("macOS");
    private final JRadioButton preferInAppMenuBar = new JRadioButton("In-App");

    public PrefsDialog(Component parent) {

        setTitle("Preferences");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        addWindowListener(this);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        initComps();
        createRadioRow("Definition choice:", preferFirstDefinition, preferToAskDefinition);
        // Add Wikipedia preference checkbox:
        useWikipedia.setToolTipText("<html>Use the first sentence of a Wikipedia article<br>" +
                "with the same title of a word if a dictionary<br>" +
                "definition is not found</html>");
        JPanel pnl = new JPanel();
        pnl.add(useWikipedia);
        add(pnl);
        // Add Menu bar option if available:
        if (System.getProperty("os.name").equals("Mac OS X"))
            createRadioRow("Menu bar style (requires restart):", preferMacMenuBar, preferInAppMenuBar);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);

    }

    private void initComps() {

        // Definition choice
        preferFirstDefinition.setSelected(Settings.prefersFirstDefinition());
        preferToAskDefinition.setSelected(!preferFirstDefinition.isSelected());
        // Wikipedia preference
        useWikipedia.setSelected(Settings.acceptsWikipediaSummary());
        // Mac menu bar choice
        preferMacMenuBar.setSelected(Settings.prefersMacMenuBar());
        preferInAppMenuBar.setSelected(!preferMacMenuBar.isSelected());

        // Set hand cursor for radio buttons
        Define4Me.initButtons(null, preferFirstDefinition, preferToAskDefinition, useWikipedia, preferInAppMenuBar);

    }

    private void createRadioRow(String labelText, JRadioButton... buttons) {

        JPanel pnl = new JPanel();
        ButtonGroup group = new ButtonGroup(); // Only one radio button can be activated per group

        JLabel label = new JLabel(labelText);
        pnl.add(label);
        for (JRadioButton button : buttons) {
            group.add(button);
            pnl.add(button);
        }

        add(pnl);

    }

    @Override
    public void windowClosing(WindowEvent e) {

        Settings.setDefinitionPreference(preferFirstDefinition.isSelected());
        Settings.setWikipediaSummaryPreference(useWikipedia.isSelected());
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
