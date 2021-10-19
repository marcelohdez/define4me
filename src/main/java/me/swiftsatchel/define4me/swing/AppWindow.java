package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class AppWindow extends JFrame implements ActionListener {

    private JFileChooser jfc; // Our file chooser instance

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

    // Right click text area menu components:
    private final JPopupMenu rightClickMenu = new JPopupMenu();
    private final JMenuItem copyAllText = new JMenuItem("Copy All");
    private final JMenuItem copySelectedText = new JMenuItem("Copy Selected");

    private final ArrayList<String> words = new ArrayList<>(); // Selected file's words
    ConcurrentHashMap<String, String> definitions = new ConcurrentHashMap<>(); // The word's definitions

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

        SwingUtilities.invokeLater(() -> jfc = new JFileChooser()); // Create file chooser after window's shown.

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
        copyAllText.addActionListener(this);
        copySelectedText.addActionListener(this);

        statusText.setEditable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setComponentPopupMenu(rightClickMenu);

        rightClickMenu.add(copyAllText);
        rightClickMenu.add(copySelectedText);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(chooseButton)) {

            chooseFile();

        } else if (e.getSource().equals(defineButton)) {

            statusText.setText(defineWords());
            defineButton.setText("Write to file");

        } else if (e.getSource().equals(copyAllText)) {

            StringSelection text = new StringSelection(statusText.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);

        } else if (e.getSource().equals(copySelectedText)) {

            StringSelection text = new StringSelection(statusText.getSelectedText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);

        } else if (e.getSource().equals(openAbout)) {
            new AboutDialog();
        } else if (e.getSource().equals(openPrefs)) {
            new PrefsDialog();
        }

    }

    private void chooseFile() {

        if (jfc != null) { // Make sure we have created the JFileChooser instance
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

            statusText.setText("The $s words in $f:\n"
                    .replace("$f", file.getName())
                    .replace("$s", String.valueOf(words.size()))
                    + listWords());

        }

    }

    private String listWords() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(words.get(i));
        }

        return sb.toString();

    }

    private String defineWords() {

        definitions.clear(); // Reset definitions

        // create a thread pool the size of how many we will use, ideally one per word, but if that's more than
        // the amount of cores available then stop at that number.
        Thread[] threadList = new Thread[ Math.min(words.size(), Runtime.getRuntime().availableProcessors()) ];
        for (int i = 0; i < threadList.length; i++) { // Create a new thread for every open spot in threadList
            int threadNumber = i; // Local value to pass to thread's for loop.

            threadList[i] = new Thread(() -> {
                for (int w = threadNumber; w < words.size(); w++) { // Start on our thread number, and get words 1 by 1
                    if (!definitions.containsKey(words.get(w))) { // If the current word has not been defined yet
                        // Default text + creating the key stops other threads from trying to define it as well.
                        definitions.put(words.get(w), "No definition found");
                        try {
                            getDefinitionOf(words.get(w));
                        } catch (Exception e) {
                            System.out.println("Couldn't find a definition for $w!"
                                    .replace("$w", words.get(w)));
                        }
                    }
                }
            });
            threadList[i].start(); // Start thread

        }

        for (Thread t : threadList) { // Wait for all threads to finish.
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String word : words) { // Append all definitions to separate lines
            sb.append(word).append(" - ").append(definitions.get(word)).append("\n");
        }

        return sb.toString();

    }

    private String getJSONText(Reader r) throws IOException {

        StringBuilder sb = new StringBuilder();
        int c; // Current character
        while ((c = r.read()) != -1) {
            sb.append((char) c);
        }
        return sb.toString();

    }

    private void getDefinitionOf(String word) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        InputStream urlStream = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word).openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlStream, StandardCharsets.UTF_8));
        String jsonText = getJSONText(reader);

        JSONArray jsonArray = (JSONArray) parser.parse(jsonText); // Get all of json
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);  // Get first use of word
        jsonArray = (JSONArray) jsonObject.get("meanings");     // Get array of "meanings"
        jsonObject = (JSONObject) jsonArray.get(0);             // Get first object of "meaning"
        jsonArray = (JSONArray) jsonObject.get("definitions");  // Get array of definitions
        jsonObject = (JSONObject) jsonArray.get(0);             // Get first definition object

        definitions.put(word, jsonObject.get("definition").toString());

    }

}
