package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.util.Settings;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * JDialog with preferences to certain aspects of the program
 */
public class PrefsDialog extends JDialog implements WindowListener {

    // Radio buttons for definition choice
    private final JRadioButton preferFirstDefinition = new JRadioButton("First");
    private final JRadioButton preferToAskDefinition = new JRadioButton("Ask If Multiple");

    // Radio buttons for grammar preference
    private final JRadioButton preferFirstGrammar = new JRadioButton("First");
    private final JRadioButton preferToAskGrammar = new JRadioButton("Ask If Multiple");

    public PrefsDialog() {

        setTitle("Preferences");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        addWindowListener(this);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        initComps();
        createRadioRow("Definition choice:", preferFirstDefinition, preferToAskDefinition);
        createRadioRow("Grammar Preference (noun, adj., etc.):", preferFirstGrammar, preferToAskGrammar);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void initComps() {

        // Definition choice
        preferFirstDefinition.setSelected(Settings.isFirstDefinitionPreferred());
        preferToAskDefinition.setSelected(!preferFirstDefinition.isSelected());

        // Grammar preference
        preferFirstGrammar.setSelected(Settings.isFirstGrammarPreferred());
        preferToAskGrammar.setSelected(!preferFirstGrammar.isSelected());

    }

    private void createRadioRow(String labelText, JRadioButton... buttons) {

        JPanel pnl = new JPanel();
        ButtonGroup group = new ButtonGroup(); // Make it so only one radio button can be activated at a time

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
        Settings.setGrammarPreference(preferFirstGrammar.isSelected());

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
