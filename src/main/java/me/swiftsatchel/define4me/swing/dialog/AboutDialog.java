package me.swiftsatchel.define4me.swing.dialog;

import me.swiftsatchel.define4me.Main;

import javax.swing.*;

public class AboutDialog extends JDialog {

    public AboutDialog(int wordAmount) {

        setTitle("About");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        createProgramAbout(wordAmount);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void createProgramAbout(int wordAmount) {

        JTextArea infoText = new JTextArea("""
                Version: $v
                Words: $w
                """
                .replace("$v", Main.VERSION)
                .replace("$w", String.valueOf(wordAmount)));

        infoText.setEditable(false);
        infoText.setAlignmentX(0.5f);

        add(infoText);

    }

}
