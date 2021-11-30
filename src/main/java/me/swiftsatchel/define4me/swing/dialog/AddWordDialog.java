package me.swiftsatchel.define4me.swing.dialog;

import me.swiftsatchel.define4me.util.WordParser;

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

        String word = WordParser.parseString(textField.getText());

        // If parsed characters are not equal to the typed in text then change to the parsed ones to show user,
        // They could then change it or add the parsed text
        if (!word.equals(textField.getText())) {

            textField.setText(word);

        } else if (!word.isBlank()) { // Else if parsed characters are not blank then accept them
            accepted = true;
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
