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
    private final JRadioButton acceptWikipediaSummary = new JRadioButton("Use");
    private final JRadioButton declineWikipediaSummary = new JRadioButton("Do not use");
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
        createRadioRow("Definition choice:", null,
                preferFirstDefinition, preferToAskDefinition);
        createRadioRow("Use Wikipedia:",
                "If enabled, when no definition is found for the given word,<br>" +
                        "Define4Me will try to use the summary of a wikipedia page<br>" +
                        "with the same title instead.",
                acceptWikipediaSummary, declineWikipediaSummary);
        if (System.getProperty("os.name").equals("Mac OS X"))
            createRadioRow("Menu bar style (requires restart):", null, preferMacMenuBar, preferInAppMenuBar);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);

    }

    private void initComps() {

        // Definition choice
        preferFirstDefinition.setSelected(Settings.prefersFirstDefinition());
        preferToAskDefinition.setSelected(!preferFirstDefinition.isSelected());
        // Wikipedia summary choice
        acceptWikipediaSummary.setSelected(Settings.acceptsWikipediaSummary());
        declineWikipediaSummary.setSelected(!acceptWikipediaSummary.isSelected());
        // Mac menu bar choice
        preferMacMenuBar.setSelected(Settings.prefersMacMenuBar());
        preferInAppMenuBar.setSelected(!preferMacMenuBar.isSelected());

        // Set hand cursor for radio buttons
        Define4Me.initButtons(null, preferFirstDefinition, preferToAskDefinition, acceptWikipediaSummary,
                declineWikipediaSummary, preferInAppMenuBar);

    }

    private void createRadioRow(String labelText, String labelToolTip, JRadioButton... buttons) {

        JPanel pnl = new JPanel();
        ButtonGroup group = new ButtonGroup(); // Only one radio button can be activated per group

        JLabel label = new JLabel(labelText);
        if (labelToolTip != null) label.setToolTipText("<html>" + labelToolTip + "</html>");
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
        Settings.setWikipediaSummaryPreference(acceptWikipediaSummary.isSelected());
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
