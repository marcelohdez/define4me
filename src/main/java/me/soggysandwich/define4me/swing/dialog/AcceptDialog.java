package me.soggysandwich.define4me.swing.dialog;

import me.soggysandwich.define4me.Define4Me;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AcceptDialog extends JDialog implements ActionListener {

    private boolean accepted = false;

    private final Component parent;
    private final JButton acceptButton;

    public AcceptDialog(Component parent, String text, String acceptButtonText, String cancelButtonText) {
        this.parent = parent;

        setTitle("Alert");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setBackground(UIManager.getColor("panel.background"));

        acceptButton = new JButton(acceptButtonText);
        JButton cancel = new JButton(cancelButtonText);

        acceptButton.addActionListener(this);
        cancel.addActionListener(this);

        JPanel buttonRow = new JPanel();
        buttonRow.add(acceptButton);
        buttonRow.add(cancel);

        add(textArea, BorderLayout.CENTER);
        add(buttonRow, BorderLayout.SOUTH);
        Define4Me.initButtons(null, acceptButton, cancel);

        pack();

    }

    public boolean accepted() {
        setLocationRelativeTo(parent);
        setVisible(true);

        return accepted;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        accepted = (e.getSource() == acceptButton);
        setVisible(false);
        dispose();

    }

}