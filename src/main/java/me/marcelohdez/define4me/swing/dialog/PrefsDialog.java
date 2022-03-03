package me.marcelohdez.define4me.swing.dialog;

import me.marcelohdez.define4me.Define4Me;
import me.marcelohdez.define4me.swing.AppWindow;
import me.marcelohdez.define4me.util.Settings;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/* JDialog with preferences to certain aspects of the program */
public class PrefsDialog extends JDialog implements WindowListener {

    // Definition preference
    private final JRadioButton preferFirstDefinition = new JRadioButton("First");
    private final JRadioButton preferToAskDefinition = new JRadioButton("Ask If Multiple");
    // Wikipedia summary preference
    private final JCheckBox useWikipedia = new JCheckBox("Use Wikipedia");
    private final JComboBox<String> wikiPreferencesBox = new JComboBox<>(new String[]{"Never", "As Backup", "Always"});
    // Mac menu bar preference
    private final JRadioButton preferMacMenuBar = new JRadioButton("macOS");
    private final JRadioButton preferInAppMenuBar = new JRadioButton("In-App");

    public PrefsDialog(AppWindow parent) {

        setTitle("Preferences");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        addWindowListener(this);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        initComps();
        createRadioRow("Definition choice:", parent.isWorkerAvailable(),
                preferFirstDefinition, preferToAskDefinition);

        // Add Wikipedia preference checkbox:
        createWikipediaRow(parent);

        // Add Menu bar option if available:
        if (Define4Me.isOnMacOS()) {
            createRadioRow("Menu bar style (requires restart):", parent.isWorkerAvailable(),
                    preferMacMenuBar, preferInAppMenuBar);
        }

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);

    }

    private void initComps() {
        // Definition choice
        preferFirstDefinition.setSelected(Settings.prefersFirstDefinition());
        preferToAskDefinition.setSelected(!preferFirstDefinition.isSelected());
        // Wikipedia preference
        wikiPreferencesBox.setSelectedIndex(Settings.wikiPreference());
        // Mac menu bar choice
        preferMacMenuBar.setSelected(Settings.prefersMacMenuBar());
        preferInAppMenuBar.setSelected(!preferMacMenuBar.isSelected());

        // Set hand cursor for radio buttons
        Define4Me.initButtons(null, preferFirstDefinition, preferToAskDefinition, useWikipedia, preferInAppMenuBar);
    }

    private void createWikipediaRow(AppWindow parent) {
        JPanel pnl = new JPanel();
        JLabel wikiPrefsLabel = new JLabel("Use Wikipedia: ");

        // html is used for the <br> tag, which creates a new line. Tooltips do not initially support line breaks.
        String tooltip = """
                <html>"As Backup" will use Wikipedia when a word is not<br>
                found in the dictionary.</html>""";

        wikiPrefsLabel.setToolTipText(tooltip);
        wikiPreferencesBox.setToolTipText(tooltip);

        wikiPreferencesBox.setEnabled(parent.isWorkerAvailable());
        pnl.add(wikiPrefsLabel);
        pnl.add(wikiPreferencesBox);

        add(pnl);
    }

    private void createRadioRow(String labelText, boolean enabled, JRadioButton... buttons) {
        JPanel pnl = new JPanel();
        ButtonGroup group = new ButtonGroup(); // Only one radio button can be activated per group

        JLabel label = new JLabel(labelText);
        pnl.add(label);
        for (JRadioButton button : buttons) {
            button.setEnabled(enabled);
            group.add(button);
            pnl.add(button);
        }

        add(pnl);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Settings.setDefinitionPreference(preferFirstDefinition.isSelected());
        Settings.setWikiPreference(wikiPreferencesBox.getSelectedIndex());
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
