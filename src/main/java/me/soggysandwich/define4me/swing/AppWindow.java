package me.soggysandwich.define4me.swing;

import me.soggysandwich.define4me.Define4Me;
import me.soggysandwich.define4me.swing.comp.MiddlePane;
import me.soggysandwich.define4me.swing.dialog.*;
import me.soggysandwich.define4me.util.Settings;
import me.soggysandwich.define4me.util.WordParser;
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
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class AppWindow extends JFrame implements KeyListener {

    private DefinitionsDialog definitionsDlg; // Dialog to choose definitions from if enabled in preferences
    private final ConcurrentHashMap<String, String> definitions = new ConcurrentHashMap<>(); // Word definitions

    // Menu bar stuffs: File menu
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenuItem openAbout = new JMenuItem("About");
    private final JMenuItem openPrefs = new JMenuItem("Preferences");

    // Right click menus: Text area
    private final JPopupMenu rightClickText = new JPopupMenu();
    private final JMenuItem copyText = new JMenuItem("Copy");
    // Words list
    private final JPopupMenu rightClickWords = new JPopupMenu();
    private final JMenuItem editWord = new JMenuItem("Edit Selected");
    private final JMenuItem pasteFromWordList = new JMenuItem("Paste");

    // Main components
    private final JButton pasteButton = new JButton("Paste");
    private final MiddlePane middlePane = new MiddlePane(this, rightClickWords, rightClickText, this); // Center tabbed pane
    private final JButton defineButton = new JButton("Define");

    private int keyBeingPressed; // Keep track of key being pressed, for ctrl/cmd + (something) shortcuts

    public AppWindow() {

        setTitle("Define4Me");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(this);

        setJMenuBar(menuBar);
        add(pasteButton, BorderLayout.WEST);
        add(middlePane, BorderLayout.CENTER);
        add(defineButton, BorderLayout.EAST);

        initComps();

        pack();
        setMinimumSize(new Dimension((int) (getWidth() * 1.2), (int) (getHeight() * 1.1)));
        setLocationRelativeTo(null); // Center on main screen
        setVisible(true);

    }

    // Initialize components
    private void initComps() {

        copyText.addActionListener((e) -> copy());
        pasteFromWordList.addActionListener((e) -> paste());
        openAbout.addActionListener((e) -> new AboutDialog(this, middlePane.wordsAmount()));
        openPrefs.addActionListener((e) -> new PrefsDialog(this));
        editWord.addActionListener((e) -> middlePane.editSelectedWord());
        middlePane.getRemoveButton().addActionListener((e) -> removeSelectedWord());

        pasteButton.addActionListener((e) -> paste());
        middlePane.getAddButton().addActionListener((e -> {
            AddWordDialog awd = new AddWordDialog(this);
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
        Define4Me.initButtons(this, pasteButton, defineButton, openAbout, openPrefs, copyText, editWord,
                pasteFromWordList);

        fileMenu.add(openAbout);
        fileMenu.add(openPrefs);
        menuBar.add(fileMenu);
        rightClickWords.add(editWord);
        rightClickWords.add(pasteFromWordList);
        rightClickText.add(copyText);

    }

    private void removeSelectedWord() {
        if (middlePane.removeSelectedWord(true))
            defineButton.setEnabled(middlePane.wordsAmount() > 0);
    }

    private void addWord(String word) {
        if (middlePane.addWord(word))
            defineButton.setEnabled(middlePane.wordsAmount() > 0);
    }

    /**
     * Uses the given scanner to find words in text
     *
     * @param reader given scanner
     */
    private void getWordsFrom(Scanner reader) {
        try (reader) {
            if (middlePane.wordsAmount() > 0) {
                String chosen = new ChoiceDialog(this, "Paste",
                    """
                    What would you like to do with
                    your existing words?""",
                        "Clear", "Add pasted")
                        .response();

                if (Objects.equals(chosen, "Cancel")) {
                    return; // Do not do anything
                } else if (Objects.equals(chosen, "Clear")) {
                    middlePane.clear();
                } // If we chose to add the words then just go ahead:
            }

            while (reader.hasNextLine()) {
                String word = WordParser.parseString(reader.nextLine());
                if (!word.isEmpty()) addWord(word); // If parsed characters are not empty then add word
            }

            middlePane.setSelectedIndex(0); // Switch back to words tab
        }
    }

    private String defineWords() {

        definitions.clear(); // Reset definitions
        if (!Settings.prefersFirstDefinition()) definitionsDlg = new DefinitionsDialog(this);

        // create a thread pool the size of how many we will use, ideally one per word, but if that's more than
        // the amount of cores available then stop at that number. (also 16 is ConcurrentHashMap's concurrent limit)
        Thread[] threadList = new Thread[
                Math.min(Runtime.getRuntime().availableProcessors(), Math.min(middlePane.wordsAmount(), 16)) ];

        summonDefineThreads(threadList);

        for (Thread t : threadList) { // Wait for all threads to finish.
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!Settings.prefersFirstDefinition()) // If enabled and having words to choose for, show dialog to select definitions:
            if (definitionsDlg.wordsToShow() > 0) definitions.putAll(definitionsDlg.getWantedDefinitions());

        StringBuilder sb = new StringBuilder();
        for (String word : middlePane.getList()) { // Append all definitions to separate lines
            sb.append(word).append(" - ").append(definitions.get(word)).append("\n");
        }

        return sb.toString();

    }

    /** Summon threads into given array where each thread will attempt to define a word */
    private void summonDefineThreads(Thread[] t) {

        for (int i = 0; i < t.length; i++) { // Create a new thread for every open spot in threadList

            int threadNumber = i; // Local value to pass to thread's for loop.
            t[i] = new Thread(() -> {
                // Start on our thread number, and get words 1 by 1
                for (int w = threadNumber; w < middlePane.wordsAmount(); w++) defineIndex(w);
            });

            t[i].start(); // Start thread
        }

    }

    private void defineIndex(int index) {

        String word = middlePane.getWordAt(index);
        if (!definitions.containsKey(word)) { // If the current word has not been defined yet
            // Default text + having a key marks this word as defined to other threads
            definitions.put(word, "No definition found");
            try {
                tryToDefineWord(word);
            } catch (Exception e) {
                System.out.println("No dictionary definition for " + word);
                if (Settings.acceptsWikipediaSummary()) tryGettingWikipediaSummary(word);
            }
        }

    }

    private String getJSONText(Reader r) throws IOException {
        try (r) {
            StringBuilder sb = new StringBuilder();
            int c; // Current character
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
            return sb.toString();
        }
    }

    /** Try to define the given string using meetDeveloper's Dictionary API */
    private void tryToDefineWord(String word) throws IOException, ParseException {
        try (var stream = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word).openStream()) {
            JSONArray jsonArray = (JSONArray) new JSONParser()
                    .parse(getJSONText(new InputStreamReader(stream, StandardCharsets.UTF_8))); // Parse response

            JSONObject jsonObject = (JSONObject) jsonArray.get(0);  // Get first use of word
            jsonArray = (JSONArray) jsonObject.get("meanings");     // Get array of "meanings"

            // If user prefers to choose between multiple definitions:
            if (!Settings.prefersFirstDefinition() && jsonArray.size() > 1) {

                definitionsDlg.addMeaningsArrayFor(word, jsonArray); // Add this word and its definitions to definitionsDlg

            } else { // Else select first meaning and definition:
                jsonObject = (JSONObject) jsonArray.get(0);             // Get first object of "meanings"
                jsonArray = (JSONArray) jsonObject.get("definitions");  // Get array of definitions
                jsonObject = (JSONObject) jsonArray.get(0);         // Get first definition object
                definitions.put(word, jsonObject.get("definition").toString()); // Add to definitions
            }
        }
    }

    /** Try to define the given string with the first sentence of a Wikipedia article with the same title */
    private void tryGettingWikipediaSummary(String w) {
        try { // Use wikipedia query API:
            try (var stream = new URL("https://en.wikipedia.org/w/api.php?format=json&action=query" +
                    "&prop=extracts&exintro&explaintext&redirects=1&titles=" + /*Replace all spaces with %20 for link:*/
                    w.replaceAll(" ", "%20")).openStream()) {

                JSONObject jsonObject = (JSONObject) new JSONParser()
                        .parse(getJSONText(new InputStreamReader(stream, StandardCharsets.UTF_8))); // Parse response

                jsonObject = (JSONObject) jsonObject.get("query"); // Get query section
                jsonObject = (JSONObject) jsonObject.get("pages"); // Get pages section
                jsonObject = (JSONObject) jsonObject.get(jsonObject.keySet().iterator().next()); // Choose first page

                String text = jsonObject.get("extract").toString();
                for (int i = 0; i < text.length() - 1; i++) { // Find the first period to cut everything after it
                    // Make sure the next char after this period is not a digit, to not cut off decimals/version numbers.
                    if (text.charAt(i) == '.' && !Character.isDigit(text.charAt(i + 1))) {
                        text = text.substring(0, i + 1); // Cut text down to this first sentence
                        break; // Leave this for-loop
                    }
                }

                definitions.put(w, text); // Put wikipedia summary as definition

            }
        } catch (Exception ex) { System.out.println("No Wikipedia entry for " + w); }
    }

    /** Gets contents from the clipboard as stringFlavor and parse it to put its words in the word list */
    private void paste() {
        try {
            String pastedText = Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor).toString();
            getWordsFrom(new Scanner(pastedText));
        } catch (IOException | UnsupportedFlavorException ex) {
            System.out.println("Unable to paste from clipboard! Stack trace:");
            ex.printStackTrace();
        }
    }

    /** Copies the contents of the middlePane's text area into the user's clipboard */
    private void copy() {
        StringSelection text;
        if (middlePane.getSelectedText() != null) { // If there is something selected get that text:
            text = new StringSelection(middlePane.getSelectedText());
        } else { // Else just get the whole text area's contents
            text = new StringSelection(middlePane.getText());
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // For ctrl--or meta/command in macOS-- + (something) shortcuts
        if (keyBeingPressed == KeyEvent.VK_CONTROL || keyBeingPressed == KeyEvent.VK_META) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_V -> paste();
                case KeyEvent.VK_C -> copy();
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
