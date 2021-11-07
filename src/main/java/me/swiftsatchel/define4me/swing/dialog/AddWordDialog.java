package me.swiftsatchel.define4me.swing.dialog;

import me.swiftsatchel.define4me.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddWordDialog extends JDialog implements ActionListener {

    private boolean accepted = false;

    protected final JTextField textField = new JTextField();
    private final JButton acceptButton = new JButton("Accept");

    public AddWordDialog() {

        setTitle("Add Word");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        JButton cancelButton = new JButton("Cancel");
        JPanel buttonRow = new JPanel();

        acceptButton.addActionListener(this);
        cancelButton.addActionListener(this);

        add(textField, BorderLayout.CENTER);
        buttonRow.add(acceptButton);
        buttonRow.add(cancelButton);
        add(buttonRow, BorderLayout.SOUTH);

        pack();

    }

    public boolean accepted() {
        setLocationRelativeTo(null);
        setVisible(true);
        return accepted;
    }

    public String getWord() {
        return textField.getText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(acceptButton)) {

            boolean allowed = true;
            for (int i = 0; i < textField.getText().length(); i++) { // Make sure all characters are allowed
                if (! (Main.ACCEPTED.contains(textField.getText().substring(i, i+1).toLowerCase()))) {
                    allowed = false;
                }
            }

            if (allowed && !textField.getText().isBlank()) { // If all characters passed check and word is not blank
                accepted = true;
                setVisible(false);
            }

        } else {

            setVisible(false);

        }

    }

}
