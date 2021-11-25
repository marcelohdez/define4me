package me.swiftsatchel.define4me.swing.dialog;

import me.swiftsatchel.define4me.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class AddWordDialog extends JDialog implements ActionListener, KeyListener {

    private boolean accepted = false;

    protected final JTextField textField = new JTextField();
    private final JButton acceptButton = new JButton("Accept");

    public AddWordDialog() {

        setTitle("Add Word");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        JButton cancelButton = new JButton("Cancel");
        JPanel buttonRow = new JPanel();

        textField.addKeyListener(this);
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

    private void acceptWord() {

        int firstLetter = -1; // Used to get rid of extra white space at start (ex: "   hello")
        for (int i = 0; i < textField.getText().length(); i++)
            if (!textField.getText().substring(i, i + 1).isBlank()) {
                firstLetter = i;
                break;
            }

        boolean allowed = true;
        for (int i = firstLetter; i < textField.getText().length(); i++) // Make sure all characters are allowed
            if (!(Main.ACCEPTED.contains(textField.getText().substring(i, i+1).toLowerCase())))
                allowed = false;

        if (allowed && !textField.getText().isBlank()) { // If all characters passed check and word is not blank
            accepted = true;
            textField.setText(textField.getText().substring(firstLetter));
            setVisible(false);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(acceptButton)) {
            acceptWord();
        } else setVisible(false);

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) acceptWord();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

}
