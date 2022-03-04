package me.marcelohdez.define4me.swing;

import me.marcelohdez.define4me.Define4Me;
import me.marcelohdez.define4me.swing.comp.MiddlePane;
import me.marcelohdez.define4me.swing.dialog.*;
import me.marcelohdez.define4me.util.Settings;
import me.marcelohdez.define4me.util.WordParser;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class AppWindow extends JFrame implements KeyListener {

    // Links to use when searching for definitions/descriptions
    private static final String DICT_QUERY = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private static final String WIKI_QUERY =
            "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&exsentences=1&explaintext&titles=";

    private DefinitionsDialog definitionsDlg; // Dialog to choose definitions from if enabled in preferences
    private final ConcurrentHashMap<String, String> definitionsArray = new ConcurrentHashMap<>(); // Word definitions
    private StringBuilder wikipediaQueue = new StringBuilder(); // Stores word indexes to look for on wikipedia

    // Menu bar stuffs: File menu
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu("Program");
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
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    // Other
    private int keyBeingPressed; // Keep track of key being pressed, for ctrl/cmd + (something) shortcuts
    private SwingWorker<String, Float> worker; // Worker to do work in background thread and update progress bar
    private int finishedWordsAmount = 0; // Keep count of defined words for progress bar

    public AppWindow() {

        setTitle("Define4Me");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addKeyListener(this); // Listen for shortcuts

        setJMenuBar(menuBar);
        add(pasteButton, BorderLayout.WEST);
        add(middlePane, BorderLayout.CENTER);
        add(defineButton, BorderLayout.EAST);
        add(progressBar, BorderLayout.SOUTH);

        initComps();

        pack();
        setMinimumSize(new Dimension((int) (getWidth() * 1.2), (int) (getHeight() * 1.1)));
        setLocationRelativeTo(null); // Center on main screen
        setVisible(true);

    }

    /** Initialize components */
    private void initComps() {

        copyText.addActionListener((e) -> copy());
        pasteFromWordList.addActionListener((e) -> paste());
        openAbout.addActionListener((e) -> new AboutDialog(this, middlePane.wordsAmount()));
        openPrefs.addActionListener((e) -> new PrefsDialog(this));
        editWord.addActionListener((e) -> middlePane.editSelectedWord());
        middlePane.getRemoveButton().addActionListener((e) -> removeSelectedWord());

        pasteButton.addActionListener((e) -> paste());
        middlePane.getAddButton().addActionListener((e -> {
            String addition = new AddWordDialog(this).getWord();
            if (!addition.isEmpty()) addWord(addition);
        }));
        defineButton.addActionListener((e) -> startWorker());

        defineButton.setEnabled(false); // Disable define button until we have words to define
        Define4Me.addHandCursorAndKLTo(this, pasteButton, defineButton, openAbout, openPrefs, copyText, editWord,
                pasteFromWordList);

        fileMenu.add(openAbout);
        fileMenu.add(openPrefs);
        menuBar.add(fileMenu);
        rightClickWords.add(editWord);
        rightClickWords.add(pasteFromWordList);
        rightClickText.add(copyText);

    }

    /** Make worker execute if it is currently pending */
    private void startWorker() {
        if (isWorkerAvailable()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Set loading cursor

            worker = new SwingWorker<>() {
                String resultText;

                @Override
                protected String doInBackground() {
                    resultText = defineWords(() -> {
                        float progressFloat = (float) finishedWordsAmount / middlePane.getList().size();
                        setProgress((int) (progressFloat * 100));
                    });
                    return resultText;
                }

                @Override
                protected void done() {
                    middlePane.setStatusText(resultText);
                    progressBar.setValue(0);
                    progressBar.setToolTipText("Done");
                    setCursor(Cursor.getDefaultCursor());
                    worker = null; // Make worker "available" again
                }
            };
            worker.addPropertyChangeListener(e -> {
                if (e.getPropertyName().equals("progress")) {
                    progressBar.setValue((Integer) e.getNewValue());
                    progressBar.setToolTipText(progressBar.getValue() + "/" + middlePane.getList().size());
                }
            });

            worker.execute();
        }
    }

    private void removeSelectedWord() {
        if (isWorkerAvailable() && middlePane.removeSelectedWord(true)) {
            defineButton.setEnabled(middlePane.wordsAmount() > 0); // Enable define button if there are words in list
        }
    }

    private void addWord(String word) {
        if (isWorkerAvailable() && middlePane.addWord(word)) {
            defineButton.setEnabled(middlePane.wordsAmount() > 0); // Enable define button if there are words in list
        }
    }

    public boolean isWorkerAvailable() {
        return worker == null;
    }

    /**
     * Uses the given scanner to find words in text
     *
     * @param reader given scanner
     */
    private void getWordsFrom(Scanner reader) {
        try (reader) {
            if (middlePane.wordsAmount() > 0) {
                String chosen = new ChoiceDialog(this, "Paste Options",
                    """
                    What would you like to do with
                    your existing words?""",
                        "Cancel", "Clear", "Add pasted")
                        .response();

                if (Objects.equals(chosen, "Cancel")) {
                    return; // Do not do anything
                } else if (Objects.equals(chosen, "Clear")) {
                    middlePane.clear();
                } // If we chose to add the words then just continue:
            }

            while (reader.hasNextLine()) {
                String word = WordParser.parseString(reader.nextLine());
                if (!word.isEmpty()) addWord(word); // If parsed characters are not empty then add word
            }

            middlePane.setSelectedIndex(0); // Switch back to words tab
        }
    }

    private String defineWords(Runnable runAfterEachWord) {
        resetValues();

        // Try to define the words
        for (int i = 0; i < middlePane.wordsAmount(); i++) {
            defineWordAt(i);
            runAfterEachWord.run();
        }
        if (wikipediaQueue.length() > 0) tryWikipediaQuery(); // If there is any words in the wiki queue, do stuff

        // If enabled and having words with multiple definitions, show chooser dialog:
        if (!Settings.prefersFirstDefinition() && definitionsDlg.wordsToShow() > 0) {
            definitionsArray.putAll(definitionsDlg.getWantedDefinitions());
        }

        // Create text to show user: (All words in new lines, with their definition after a dash)
        StringBuilder finalText = new StringBuilder();
        for (String word : middlePane.getList()) { // Append all definitions to separate lines
            finalText.append(word).append(" - ").append(definitionsArray.get(word)).append("\n");
        }

        return finalText.toString();
    }

    private void resetValues() {
        definitionsArray.clear(); // Reset definitions
        finishedWordsAmount = 0; // Reset finished word count
        wikipediaQueue = new StringBuilder();
        if (!Settings.prefersFirstDefinition()) definitionsDlg = new DefinitionsDialog(this);
    }

    private void defineWordAt(int index) {
        String word = middlePane.wordAt(index);

        definitionsArray.put(word, "No definition found");
        try {
            if (Settings.isWikiPrefAlways()) {
                appendToWikiQueue(word);
            } else {
                tryDefining(word);
            }
        } catch (Exception e) {
            System.out.println("No dictionary definition for " + word);
            if (Settings.isWikiPrefAsBackup()) {
                appendToWikiQueue(word);
            } else if (Settings.isWikiPrefNever()) {
                System.out.println("Not checking Wikipedia for " + word + " as the preference is set to never!");
            }
        }
        finishedWordsAmount ++; // Add to count of "finished" words for progress bar
    }

    private void appendToWikiQueue(String word) {
        // If we have added a word before, separate each new one
        if (wikipediaQueue.length() > 0) wikipediaQueue.append("|");

        wikipediaQueue.append(word);
    }

    /** Try to define the given string using meetDeveloper's Dictionary API */
    private void tryDefining(String word) throws IOException, ParseException {
        try (var stream = new URL(DICT_QUERY + word).openStream()) {

            var jsonArray = (JSONArray) parseStream(stream); // Parse response
            var jsonObject = (JSONObject) jsonArray.get(0);  // Get first use of word
            jsonArray = (JSONArray) jsonObject.get("meanings");     // Get array of "meanings"

            // If user prefers to choose between multiple definitions:
            if (!Settings.prefersFirstDefinition() && jsonArray.size() > 1) {

                definitionsDlg.addMeaningsArrayFor(word, jsonArray); // Add this word and its definitions to choose from

            } else { // Else select first meaning and definition:
                jsonObject = (JSONObject) jsonArray.get(0);             // Get first object of "meanings"
                jsonArray = (JSONArray) jsonObject.get("definitions");  // Get array of definitions
                jsonObject = (JSONObject) jsonArray.get(0);         // Get first definition object
                definitionsArray.put(word, jsonObject.get("definition").toString()); // Add to definitions
            }
        }
    }

    /** Try to define the given string with the first sentence of a Wikipedia article with the same title */
    private void tryWikipediaQuery() {
        String words = wikipediaQueue.toString();
        // Use the wikipedia query: also change all spaces to %20 for link to work
        try (var stream = new URL(WIKI_QUERY + words.replaceAll(" ", "%20"))
                .openStream()) {

            System.out.println(words);
            var jsonObject = (JSONObject) parseStream(stream); // Parse response
            jsonObject = (JSONObject) jsonObject.get("query"); // Get query section
            jsonObject = (JSONObject) jsonObject.get("pages"); // Get pages section

            for (Object pageNumber : jsonObject.keySet()) {
                JSONObject pageJSON = (JSONObject) jsonObject.get(pageNumber);
                // Map definition to page title
                String def = pageJSON.get("extract").toString();
                if (!def.isEmpty()) {
                    definitionsArray.put(pageJSON.get("title").toString(), def);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("No Wikipedia entry for " + words);
        }
    }

    private Object parseStream(InputStream stream) throws IOException, ParseException {
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            int c; // Current character
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }

            return new JSONParser().parse(sb.toString());
        }
    }

    /** Gets contents from the clipboard as stringFlavor and parse it to put its words in the word list */
    private void paste() {
        if (isWorkerAvailable()) {
            try {
                String pastedText = Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getData(DataFlavor.stringFlavor).toString();
                getWordsFrom(new Scanner(pastedText));
            } catch (IOException | UnsupportedFlavorException ex) {
                System.out.println("Unable to paste from clipboard! Stack trace:");
                ex.printStackTrace();
            }
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
