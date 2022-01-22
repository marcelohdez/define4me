package me.soggysandwich.define4me.swing.dialog;

import me.soggysandwich.define4me.Define4Me;
import me.soggysandwich.define4me.util.WordParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class AddWordDialog extends JDialog implements KeyListener {

    private final Component parent;
    protected final JTextField textField = new JTextField();

    public AddWordDialog(Component parent) {
        this.parent = parent;

        setTitle("Add Word");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        JPanel buttonRow = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        JButton acceptButton = new JButton("Accept");

        textField.addKeyListener(this);
        acceptButton.addActionListener((e) -> acceptWord());
        cancelButton.addActionListener((e) -> setVisible(false));

        add(textField, BorderLayout.CENTER);
        buttonRow.add(acceptButton);
        buttonRow.add(cancelButton);
        add(buttonRow, BorderLayout.SOUTH);
        Define4Me.initButtons(null, acceptButton, cancelButton);

        pack();

    }

    public String getWord() {
        setLocationRelativeTo(parent);
        setVisible(true);

        return textField.getText();
    }

    private void acceptWord() {
        String word = WordParser.parseString(textField.getText());

        // If parsed characters are not equal to the typed in text then change to the parsed ones to show user,
        // They could then change it or add the parsed text
        if (!word.equals(textField.getText())) {
            textField.setText(word);
        } else {
            setVisible(false);
            dispose();
        }
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
