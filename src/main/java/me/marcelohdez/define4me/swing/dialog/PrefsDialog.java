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

        initComps(parent.isWorkerAvailable());
        createRadioRow("Definition choice:", preferFirstDefinition, preferToAskDefinition);
        createWikipediaRow(parent);
        // Add Menu bar option if available:
        if (Define4Me.isOnMacOS()) {
            createRadioRow("Menu bar style (requires restart):", preferMacMenuBar, preferInAppMenuBar);
        }

        pack();
        setLocationRelativeTo(parent); // Center on parent window
        setVisible(true);

    }

    private void initComps(boolean isWorkerAvailable) {
        // Definition choice
        preferFirstDefinition.setSelected(Settings.prefersFirstDefinition());
        preferToAskDefinition.setSelected(!preferFirstDefinition.isSelected());

        boolean enableDefPrefs = isWorkerAvailable && Settings.wikiPreference() != Settings.WIKI_PREF_ALWAYS;
        preferFirstDefinition.setEnabled(enableDefPrefs); // Disable if wikipedia is always being used
        preferToAskDefinition.setEnabled(enableDefPrefs);
        // Wikipedia preference
        wikiPreferencesBox.setSelectedIndex(Settings.wikiPreference());
        wikiPreferencesBox.setEnabled(isWorkerAvailable);
        // Mac menu bar choice
        preferMacMenuBar.setSelected(Settings.prefersMacMenuBar());
        preferInAppMenuBar.setSelected(!preferMacMenuBar.isSelected());

        // Set hand cursor for radio buttons
        Define4Me.addHandCursorAndKLTo(null, preferFirstDefinition, preferToAskDefinition, wikiPreferencesBox, preferInAppMenuBar);
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
        wikiPreferencesBox.addItemListener(e -> {
            boolean enable = e.getItem() != "Always";
            preferFirstDefinition.setEnabled(enable);
            preferToAskDefinition.setEnabled(enable);
        });
        pnl.add(wikiPrefsLabel);
        pnl.add(wikiPreferencesBox);

        add(pnl);
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
