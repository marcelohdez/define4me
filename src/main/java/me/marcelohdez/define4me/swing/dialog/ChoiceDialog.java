package me.marcelohdez.define4me.swing.dialog;

import me.marcelohdez.define4me.Define4Me;

import javax.swing.*;
import java.awt.*;

public class ChoiceDialog extends JDialog {

    private String response;

    public ChoiceDialog(Component summoner, String title, String desc, String choice, String... otherChoices) {
        setTitle(title);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel buttonRow = new JPanel();
        addButtonTo(buttonRow, "Cancel");
        addButtonTo(buttonRow, choice);
        for (String s : otherChoices) addButtonTo(buttonRow, s);

        JTextArea textArea = new JTextArea(desc);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setBackground(UIManager.getColor("Panel.background")); // Blend in with JPanel
        textArea.setMargin(new Insets(8, 8, 8, 8)); // Give padding

        add(textArea, BorderLayout.NORTH);
        add(buttonRow, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(summoner);
    }

    private void addButtonTo(JPanel to, String buttonText) {
        JButton button = new JButton(buttonText);
        Define4Me.initButtons(null, button);
        button.addActionListener(e -> {
            response = buttonText;
            setVisible(false);
            dispose();
        });
        to.add(button);
    }

    public String response() {
        setModalityType(ModalityType.APPLICATION_MODAL);
        setVisible(true);

        return response;
    }

}
