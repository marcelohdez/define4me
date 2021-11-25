package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.Main;
import me.swiftsatchel.define4me.swing.comp.MiddlePane;
import me.swiftsatchel.define4me.swing.dialog.*;
import me.swiftsatchel.define4me.util.Settings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class AppWindow extends JFrame implements KeyListener {

    private final ConcurrentHashMap<String, String> definitions = new ConcurrentHashMap<>(); // Word definitions

    // Menu bar stuffs: File menu
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuItem openAbout = new JMenuItem("About");
    private final JMenuItem openPrefs = new JMenuItem("Preferences");
    // Words menu
    private final JMenu wordsMenu = new JMenu("Words");
    private final JMenuItem pasteText = new JMenuItem("Paste Text");

    // Right click menus: Text area
    private final JPopupMenu rightClickText = new JPopupMenu();
    private final JMenuItem copyAllText = new JMenuItem("Copy All");
    private final JMenuItem copySelectedText = new JMenuItem("Copy Selected");
    // Words list
    private final JPopupMenu rightClickWords = new JPopupMenu();
    private final JMenuItem editWord = new JMenuItem("Edit");

    // Main components
    private final MiddlePane middlePane = new MiddlePane(this, rightClickWords, rightClickText, this); // Center tabbed pane
    private final JButton defineButton = new JButton("Define");

    private int keyBeingPressed; // Keep track of key being pressed, for ctrl/cmd + (something) shortcuts

    public AppWindow() {

        setTitle("Define4Me");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(this);

        setJMenuBar(menuBar);
        add(middlePane, BorderLayout.CENTER);
        add(defineButton, BorderLayout.EAST);

        initComps();

        pack();
        setMinimumSize(new Dimension((int) (getWidth() * 1.2), (int) (getHeight() * 1.2)));
        setLocationRelativeTo(null); // Center on main screen
        setVisible(true);

    }

    // Initialize components
    private void initComps() {

        copyAllText.addActionListener((e) -> copy(new StringSelection(middlePane.getStatusText())));
        copySelectedText.addActionListener((e) -> copy(new StringSelection(middlePane.getSelectedStatusText())));
        pasteText.addActionListener((e) -> paste());
        openAbout.addActionListener((e) -> new AboutDialog());
        openPrefs.addActionListener((e) -> new PrefsDialog());
        editWord.addActionListener((e) -> middlePane.editSelectedWord());
        middlePane.getRemoveButton().addActionListener((e) -> removeSelectedWord());

        middlePane.getAddButton().addActionListener((e -> {
            AddWordDialog awd = new AddWordDialog();
            if (awd.accepted()) addWord(awd.getWord());
            awd.dispose();
        }));
        defineButton.addActionListener((e) -> {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Set loading cursor
            middlePane.setStatusText(defineWords());
            middlePane.setSelectedIndex(1); // Switch to definitions tab
            this.setCursor(Cursor.getDefaultCursor()); // Remove loading cursor
        });

        defineButton.setEnabled(false); // Disable define button until we have words to define
        initButtons(defineButton, openAbout, openPrefs, pasteText, copyAllText, copySelectedText, editWord);

        fileMenu.add(openAbout);
        fileMenu.add(openPrefs);
        wordsMenu.add(pasteText);
        menuBar.add(fileMenu);
        menuBar.add(wordsMenu);
        rightClickWords.add(editWord);
        rightClickText.add(copyAllText);
        rightClickText.add(copySelectedText);

    }

    /**
     * Initializes buttons with this as KeyListener then gives them a hand cursor button
     * @param buttons Buttons to initialize
     */
    public void initButtons(AbstractButton... buttons) {
        for (AbstractButton b : buttons) {
            b.addKeyListener(this);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private void removeSelectedWord() {
        if (middlePane.removeSelectedWord(true))
            defineButton.setEnabled(middlePane.getWordsSize() > 0);
    }

    private void addWord(String word) {
        if (middlePane.addWord(word))
            defineButton.setEnabled(middlePane.getWordsSize() > 0);
    }

    /**
     * Uses the given scanner to find words in text
     *
     * @param reader given scanner
     */
    private void getWordsFrom(Scanner reader) {

        if (middlePane.getWordsSize() > 0)
            if (new AcceptDialog("""
                    Would you like to clear your
                    current list of words or add
                    to them?""", "Clear", "Add")
                    .accepted())
                middlePane.clear();

        StringBuilder wordBuilder;

        while (reader.hasNextLine()) {

            wordBuilder = new StringBuilder();
            String line = reader.nextLine(); // Store next line in string

            int firstLetter = -1; // Used to get rid of extra white space at start (ex: "   hello")
            for (int i = 0; i < line.length(); i++)
                if (!line.substring(i, i+1).isBlank()) {
                    firstLetter = i;
                    break;
                }

            for (int i = firstLetter; i < line.length(); i++) // Add every accepted character
                if (Main.ACCEPTED.contains(line.substring(i, i+1).toLowerCase())) {
                    // If current and next character is " -" go to next line, for text with hyphens after words
                    if (line.startsWith(" -", i)) break;
                    wordBuilder.append(line.charAt(i)); // Else add the current character
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
                Math.min(Runtime.getRuntime().availableProcessors(), Math.min(middlePane.getWordsSize(), 16)) ];

        for (int i = 0; i < threadList.length; i++) { // Create a new thread for every open spot in threadList
            int threadNumber = i; // Local value to pass to thread's for loop.

            threadList[i] = new Thread(() -> {
                for (int w = threadNumber; w < middlePane.getWordsSize(); w++) { // Start on our thread number, and get words 1 by 1
                    if (!definitions.containsKey(middlePane.getWordAt(w))) { // If the current word has not been defined yet
                        // Default text + creating the key stops other threads from trying to define it as well.
                        definitions.put(middlePane.getWordAt(w), "No definition found");
                        try {
                            getDefinitionOf(middlePane.getWordAt(w));
                        } catch (Exception e) {
                            System.out.println("Couldn't find a definition for $w!"
                                    .replace("$w", middlePane.getWordAt(w)));
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
        for (String word : middlePane.getList()) { // Append all definitions to separate lines
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
            middlePane.setSelectedIndex(0); // Switch back to words tab
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
        // For ctrl--or meta/command in macOS-- + (something) shortcuts
        if (keyBeingPressed == KeyEvent.VK_CONTROL || keyBeingPressed == KeyEvent.VK_META) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_V -> paste();
                case KeyEvent.VK_C -> copy(new StringSelection(middlePane.getStatusText()));
            }
        } else // Else check for individual key shortcuts:
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) removeSelectedWord();

        keyBeingPressed = e.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyBeingPressed = -1;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

}
