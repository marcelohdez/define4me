package me.swiftsatchel.define4me.swing.dialog;

import me.swiftsatchel.define4me.Define4Me;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

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
        // Info text:
        JTextArea infoText = new JTextArea("""
                Program version: $v
                
                Words on list: $w
                """
                .replace("$v", Define4Me.VERSION)
                .replace("$w", String.valueOf(wordAmount)));

        infoText.setEditable(false);
        infoText.setAlignmentX(0.5f);
        infoText.setMargin(new Insets(8, 8, 8, 8));
        // Copy version right-click menu:
        JPopupMenu copyMenu = new JPopupMenu();
        JMenuItem copyVersion = new JMenuItem("Copy version");

        copyMenu.add(copyVersion);
        Define4Me.initButtons(null, copyVersion);
        copyVersion.addActionListener((e) -> {
            StringSelection text = new StringSelection("v" + Define4Me.VERSION);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);
        });

        infoText.setComponentPopupMenu(copyMenu); // Add right-click menu to infoText
        add(infoText); // Add infoText to window

    }

}
