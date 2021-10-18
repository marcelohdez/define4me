package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.Main;
import me.swiftsatchel.define4me.util.Define;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class AppWindow extends JFrame implements ActionListener {

    // Main components
    private final JButton chooseButton = new JButton("Choose file");
    private final JButton defineButton = new JButton("Define");

    private final JTextArea statusText = new JTextArea("""
            No file selected.
            Words found in the selected file will be shown here
            """);

    // Top menu bar components:
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuItem openAbout = new JMenuItem("About");
    private final JMenuItem openPrefs = new JMenuItem("Preferences");

    private final ArrayList<String> words = new ArrayList<>(); // Selected file's words

    public AppWindow() {

        setTitle("Define4Me");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setJMenuBar(menuBar);
        add(chooseButton, BorderLayout.WEST);
        JScrollPane pane = new JScrollPane(statusText);
        add(pane, BorderLayout.CENTER);
        add(defineButton, BorderLayout.EAST);

        initComps();

        pack();
        setMinimumSize(new Dimension((int) (getWidth()*1.5), (int) (getHeight()*1.5)));
        setLocationRelativeTo(null); // Center on main screen
        setVisible(true);

    }

    // Initialize components
    private void initComps() {

        fileMenu.add(openAbout);
        fileMenu.add(openPrefs);
        menuBar.add(fileMenu);

        chooseButton.addActionListener(this);
        defineButton.addActionListener(this);
        defineButton.setEnabled(false); // Disable define button until we have selected a file
        openPrefs.addActionListener(this);
        openAbout.addActionListener(this);

        statusText.setEditable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == chooseButton) {

            JFileChooser jfc = new JFileChooser(); // Create a new JFileChooser instance
            jfc.setFileFilter(new FileNameExtensionFilter("Text Files", "txt")); // Set file filter
            int returnVal = jfc.showDialog(this, "Define"); // Get return value of it

            if (returnVal == JFileChooser.APPROVE_OPTION) { // If file was approved
                try {
                    readFile(jfc.getSelectedFile());    // Try to read it
                } catch (Exception x) {
                    System.out.println("Failed to read file! Stack trace:");
                    x.printStackTrace();
                }
            }

        } else if (e.getSource() == defineButton) {

            statusText.setText(Define.allWords(words));
            defineButton.setText("Write to file");

        } else if (e.getSource() == openAbout) {
            new AboutDialog();
        } else if (e.getSource() == openPrefs) {
            new PrefsDialog();
        }

    }

    private void readFile(File file) throws FileNotFoundException {

            if (file.exists()) {

                words.clear(); // Clear list
                Scanner reader = new Scanner(file);
                StringBuilder wordBuilder;

                while (reader.hasNextLine()) {

                    wordBuilder = new StringBuilder();
                    String nextLine = reader.nextLine(); // Store next line in string

                    for (int i = 0; i < nextLine.length(); i++) {  // Go through every character in string
                        // Check if it is accepted
                        if (Main.ACCEPTED.contains(nextLine.substring(i, i+1).toLowerCase())) {

                            if (nextLine.startsWith(" -", i)) { // If current and next character is " -"
                                break; // Go to next line, for lists written with hyphens after words
                            }
                            wordBuilder.append(nextLine.charAt(i));

                        }
                    }

                    words.add(wordBuilder.toString());
                }

                statusText.setText("Words in " + file.getName() + ":\n" + wordsList());
                defineButton.setEnabled(true);

            }

    }

    private String wordsList() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(words.get(i));
        }

        return sb.toString();

    }

}
