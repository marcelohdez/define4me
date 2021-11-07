package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.Main;
import me.swiftsatchel.define4me.swing.dialog.AboutDialog;
import me.swiftsatchel.define4me.swing.dialog.AddWordDialog;
import me.swiftsatchel.define4me.swing.dialog.EditWordDialog;
import me.swiftsatchel.define4me.swing.dialog.PrefsDialog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class AppWindow extends JFrame implements ActionListener, KeyListener {

    private JFileChooser jfc; // Our file chooser instance
    private final ArrayList<String> wordsArray = new ArrayList<>(); // Current list of words
    private final ConcurrentHashMap<String, String> definitions = new ConcurrentHashMap<>(); // Word definitions

    // Main components
    private final JButton chooseButton = new JButton("Choose file");
    private final JButton defineButton = new JButton("Define");

    private final JButton removeButton = new JButton("Remove");
    private final JButton addButton = new JButton("Add");
    private final DefaultListModel<String> words = new DefaultListModel<>();
    private final JList<String> wordList = new JList<>(words);
    private final JTextArea statusText = new JTextArea("""
            No words have been defined yet.
            """);

    private final JTabbedPane centerTabbedPane = new JTabbedPane();
    // Menu bar stuffs: File menu
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuItem openAbout = new JMenuItem("About");
    private final JMenuItem openPrefs = new JMenuItem("Preferences");
    // Words menu
    private final JMenu wordsMenu = new JMenu("Words");
    private final JMenuItem pasteText = new JMenuItem("Paste Text");
    // Right click menus: Text area
    private final JPopupMenu rightClickMenu = new JPopupMenu();
    private final JMenuItem copyAllText = new JMenuItem("Copy All");
    private final JMenuItem copySelectedText = new JMenuItem("Copy Selected");
    // Words list
    private final JPopupMenu rightClickWords = new JPopupMenu();
    private final JMenuItem editWord = new JMenuItem("Edit");

    private int keyBeingPressed;

    public AppWindow() {

        setTitle("Define4Me");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(this);

        setJMenuBar(menuBar);
        add(chooseButton, BorderLayout.WEST);
        centerTabbedPane.addTab("Words", createListAndButtonsPanel());
        centerTabbedPane.addTab("Definitions", new JScrollPane(statusText));
        add(centerTabbedPane, BorderLayout.CENTER);
        add(defineButton, BorderLayout.EAST);

        initComps();

        pack();
        setMinimumSize(getSize());
        setSize(new Dimension((int) (getWidth()*1.2), (int) (getHeight()*1.2)));
        setLocationRelativeTo(null); // Center on main screen
        setVisible(true);

        SwingUtilities.invokeLater(() -> jfc = new JFileChooser()); // Create file chooser after window's shown.

    }

    private JPanel createListAndButtonsPanel() {

        JPanel pnl = new JPanel();

        pnl.setLayout(new BorderLayout());
        pnl.add(new JScrollPane(wordList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        pnl.add(buttonPanel, BorderLayout.SOUTH);

        return pnl;

    }

    // Initialize components
    private void initComps() {

        fileMenu.add(openAbout);
        fileMenu.add(openPrefs);
        wordsMenu.add(pasteText);
        menuBar.add(fileMenu);
        menuBar.add(wordsMenu);

        defineButton.setEnabled(false); // Disable define button until we have words to define
        initButtons(chooseButton, defineButton, removeButton, addButton, openAbout, openPrefs,
                pasteText, copyAllText, copySelectedText, editWord);

        statusText.setEditable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setComponentPopupMenu(rightClickMenu);

        wordList.addKeyListener(this);
        rightClickWords.add(editWord);
        wordList.setComponentPopupMenu(rightClickWords);

        rightClickMenu.add(copyAllText);
        rightClickMenu.add(copySelectedText);

    }

    /**
     * Adds this as ActionLister and sets cursor to hand cursor on the given abstract buttons.
     * @param buttons Buttons to add this as ActionListener and hand cursor on
     */
    private void initButtons(AbstractButton... buttons) {
        for (AbstractButton b : buttons) {
            b.addActionListener(this);
            b.addKeyListener(this);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void removeSelectedWord() {

        if (!wordList.isSelectionEmpty()) {
            int index = wordList.getSelectedIndex();
            wordsArray.remove(index);
            words.remove(index);
            wordList.setSelectedIndex(index - 1); // Keep selection on one index below
            defineButton.setEnabled(wordsArray.size() > 0);
        }

    }

    private void editSelectedWord() {

        if (!wordList.isSelectionEmpty()) {
            int index = wordList.getSelectedIndex();
            EditWordDialog ewd = new EditWordDialog(words.get(index));
            if (ewd.accepted()) {
                removeSelectedWord();
                addWordAt(ewd.getWord(), index);
            }
            ewd.dispose();
        }

    }

    private void addWord(String word) {
        if (!wordsArray.contains(word)) { // Avoid adding a duplicate
            wordsArray.add(word);
            words.addElement(word);
            defineButton.setEnabled(wordsArray.size() > 0);
        }
    }

    private void addWordAt(String word, int index) {
        if (!wordsArray.contains(word)) { // Avoid adding a duplicate
            wordsArray.add(index, word);
            words.add(index, word);
            defineButton.setEnabled(wordsArray.size() > 0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(chooseButton)) {

            chooseFile();

        } else if (e.getSource().equals(defineButton)) {

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Set loading cursor
            statusText.setText(defineWords());
            centerTabbedPane.setSelectedIndex(1); // Switch to definitions tab
            this.setCursor(Cursor.getDefaultCursor()); // Remove loading cursor

        } else if (e.getSource().equals(removeButton)) {

            removeSelectedWord();

        } else if (e.getSource().equals(addButton)) {

            AddWordDialog awd = new AddWordDialog();
            if (awd.accepted()) addWord(awd.getWord());
            awd.dispose();

        } else if (e.getSource().equals(copyAllText)) {

            copy(new StringSelection(statusText.getText()));

        } else if (e.getSource().equals(copySelectedText)) {

            copy(new StringSelection(statusText.getSelectedText()));

        } else if (e.getSource().equals(pasteText)) {

            paste();

        } else if (e.getSource().equals(openAbout)) {
            new AboutDialog();
        } else if (e.getSource().equals(openPrefs)) {
            new PrefsDialog();
        } else if (e.getSource().equals(editWord)) {

            editSelectedWord();

        }

    }

    private void chooseFile() {

        if (jfc != null) { // Make sure we have created the JFileChooser instance
            jfc.setFileFilter(new FileNameExtensionFilter("Text Files", "txt")); // Set file filter
            int returnVal = jfc.showDialog(this, "Define"); // Get return value of it

            if (returnVal == JFileChooser.APPROVE_OPTION) { // If file was approved
                try { // Try to read it
                    if (jfc.getSelectedFile().exists()) {
                        Scanner scanner = new Scanner(jfc.getSelectedFile());
                        getWordsFrom(scanner);
                    }
                } catch (Exception x) {
                    System.out.println("Failed to read file! Stack trace:");
                    x.printStackTrace();
                }
            }
        }

    }

    /**
     * Uses the given scanner to find words in text
     *
     * @param reader given scanner
     */
    private void getWordsFrom(Scanner reader) {

        wordsArray.clear(); // Clear list
        words.clear();
        StringBuilder wordBuilder;

        while (reader.hasNextLine()) {

            wordBuilder = new StringBuilder();
            String nextLine = reader.nextLine(); // Store next line in string

            boolean hasReachedLetter = false; // Used to get rid of extra white space at start (ex: "   hello")
            for (int i = 0; i < nextLine.length(); i++) {  // Go through every character in string
                // If we reached a letter hasReachedLetter is true
                if (!nextLine.substring(i, i+1).isBlank()) {
                    hasReachedLetter = true;
                } else { // else if we are at a blank and haven't reached a letter before, move on to next character
                    if (!hasReachedLetter) break;
                }

                // Check if it is accepted
                if (Main.ACCEPTED.contains(nextLine.substring(i, i+1).toLowerCase())) {
                    if (nextLine.startsWith(" -", i)) { // If current and next character is " -"
                        break; // Go to next line, for lists written with hyphens after words
                    }
                    wordBuilder.append(nextLine.charAt(i));
                }
            }

            String word = wordBuilder.toString();
            if (!word.isBlank()) addWord(word); // If word si not blank then add it
        }

    }

    private String defineWords() {

        definitions.clear(); // Reset definitions

        // create a thread pool the size of how many we will use, ideally one per word, but if that's more than
        // the amount of cores available then stop at that number. (also 16 is ConcurrentHashMap's concurrent limit)
        Thread[] threadList = new Thread[
                Math.min(Runtime.getRuntime().availableProcessors(), Math.min(wordsArray.size(), 16)) ];

        for (int i = 0; i < threadList.length; i++) { // Create a new thread for every open spot in threadList
            int threadNumber = i; // Local value to pass to thread's for loop.

            threadList[i] = new Thread(() -> {
                for (int w = threadNumber; w < wordsArray.size(); w++) { // Start on our thread number, and get words 1 by 1
                    if (!definitions.containsKey(wordsArray.get(w))) { // If the current word has not been defined yet
                        // Default text + creating the key stops other threads from trying to define it as well.
                        definitions.put(wordsArray.get(w), "No definition found");
                        try {
                            getDefinitionOf(wordsArray.get(w));
                        } catch (Exception e) {
                            System.out.println("Couldn't find a definition for $w!"
                                    .replace("$w", wordsArray.get(w)));
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
        for (String word : wordsArray) { // Append all definitions to separate lines
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

    private void paste() {
        try {
            String pastedText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            getWordsFrom(new Scanner(pastedText));
            centerTabbedPane.setSelectedIndex(0); // Switch back to words tab
        } catch (IOException | UnsupportedFlavorException ex) {
            System.out.println("Unable to paste from clipboard! Stack trace:");
            ex.printStackTrace();
        }
    }

    private void copy(StringSelection text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {
            case KeyEvent.VK_V -> {
                if (keyBeingPressed == KeyEvent.VK_CONTROL || keyBeingPressed == KeyEvent.VK_META) paste();
            }
            case KeyEvent.VK_C -> {
                if (keyBeingPressed == KeyEvent.VK_CONTROL || keyBeingPressed == KeyEvent.VK_META)
                    copy(new StringSelection(statusText.getText()));
            }
            case KeyEvent.VK_BACK_SPACE -> removeSelectedWord();
        }

        keyBeingPressed = e.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyBeingPressed = -1;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

}
