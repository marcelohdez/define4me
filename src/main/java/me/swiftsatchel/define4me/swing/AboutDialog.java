package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.Main;

import javax.swing.*;

public class AboutDialog extends JDialog {

    public AboutDialog() {

        setTitle("About");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        createProgramAbout();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void createProgramAbout() {

        JTextArea infoText = new JTextArea("""
                Version: $v
                """
                .replace("$v", Main.VERSION));

        infoText.setEditable(false);
        infoText.setAlignmentX(0.5f);

        add(infoText);

    }

}
