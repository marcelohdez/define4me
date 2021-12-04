package me.swiftsatchel.define4me.swing;

import me.swiftsatchel.define4me.swing.comp.MiddlePane;
import me.swiftsatchel.define4me.swing.dialog.*;
import me.swiftsatchel.define4me.util.Init;
import me.swiftsatchel.define4me.util.Settings;
import me.swiftsatchel.define4me.util.WordParser;
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
        openAbout.addActionListener((e) -> new AboutDialog(middlePane.wordsAmount()));
        openPrefs.addActionListener((e) -> new PrefsDialog());
        editWord.addActionListener((e) -> middlePane.editSelectedWord());
        middlePane.getRemoveButton().addActionListener((e) -> removeSelectedWord());

        pasteButton.addActionListener((e) -> paste());
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
        Init.buttons(this, pasteButton, defineButton, openAbout, openPrefs, copyText, editWord,
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

        if (middlePane.wordsAmount() > 0)
            if (new AcceptDialog("""
                    Would you like to clear your
                    current list of words or add
                    to them?""", "Clear", "Add")
                    .accepted())
                middlePane.clear();

        while (reader.hasNextLine()) {
            String word = WordParser.parseString(reader.nextLine());
            if (!word.isEmpty()) addWord(word); // If parsed characters are not empty then add word
        }

    }

    private String defineWords() {

        definitions.clear(); // Reset definitions
        if (!Settings.prefersFirstDefinition()) definitionsDlg = new DefinitionsDialog();

        // create a thread pool the size of how many we will use, ideally one per word, but if that's more than
        // the amount of cores available then stop at that number. (also 16 is ConcurrentHashMap's concurrent limit)
        Thread[] threadList = new Thread[
                Math.min(Runtime.getRuntime().availableProcessors(), Math.min(middlePane.wordsAmount(), 16)) ];

        for (int i = 0; i < threadList.length; i++) { // Create a new thread for every open spot in threadList
            int threadNumber = i; // Local value to pass to thread's for loop.

            threadList[i] = new Thread(() -> {
                for (int w = threadNumber; w < middlePane.wordsAmount(); w++) { // Start on our thread number, and get words 1 by 1
                    if (!definitions.containsKey(middlePane.getWordAt(w))) { // If the current word has not been defined yet
                        // Default text + having a key marks this word as defined to other threads
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

        if (!Settings.prefersFirstDefinition()) // If enabled and having words to choose for, show dialog to select definitions:
            if (definitionsDlg.wordsToShow() > 0) definitions.putAll(definitionsDlg.getWantedDefinitions());

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

    private void copy() {
        StringSelection text;
        if (middlePane.getSelectedText() != null) { // If there is something selected get that text:
            text = new StringSelection(middlePane.getSelectedText());
        } else {    // Else just get the whole text area's contents
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
