package me.marcelohdez.define4me.swing.comp;

import me.marcelohdez.define4me.Define4Me;
import me.marcelohdez.define4me.swing.AppWindow;
import me.marcelohdez.define4me.swing.dialog.EditWordDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Locale;

public class MiddlePane extends JTabbedPane {

    private final ArrayList<String> wordsArray = new ArrayList<>(); // Current list of words

    private final JButton removeButton = new JButton("Remove");
    private final JButton addButton = new JButton("Add");
    private final DefaultListModel<String> words = new DefaultListModel<>();
    private final JList<String> wordList = new JList<>(words);
    private final JTextArea statusText = new JTextArea("No words have been defined yet.");

    public MiddlePane(AppWindow app, JPopupMenu wordsMenu, JPopupMenu textMenu, KeyListener kl) {
        addTab("Words", createListAndButtonsPanel());
        addTab("Definitions", new JScrollPane(statusText));

        initComps(app, wordsMenu, textMenu, kl);
    }

    private void initComps(AppWindow app, JPopupMenu wordsMenu, JPopupMenu textMenu, KeyListener kl) {

        statusText.setEditable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setComponentPopupMenu(wordsMenu);

        wordList.addKeyListener(kl);
        wordList.setComponentPopupMenu(wordsMenu);

        statusText.addKeyListener(kl);
        statusText.setComponentPopupMenu(textMenu);

        Define4Me.initButtons(app, addButton, removeButton);

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

    public JButton getRemoveButton() {
        return removeButton;
    }

    public JButton getAddButton() {
        return addButton;
    }

    public int wordsAmount() {
        return wordsArray.size();
    }

    public String getWordAt(int i) {
        return wordsArray.get(i);
    }

    public ArrayList<String> getList() {
        return wordsArray;
    }

    public boolean removeSelectedWord(boolean reselect) {

        if (!wordList.isSelectionEmpty()) {

            int index = wordList.getSelectedIndex();
            wordsArray.remove(index);
            words.remove(index);
            if (reselect) wordList.setSelectedIndex(index - 1); // Keep selection on one index below
            return true;

        } else return false;

    }

    public boolean addWord(String word) {
        if (arrayDoesNotHave(word)) {
            wordsArray.add(word);
            words.addElement(word);
            return true; // Adding the word was successful
        } else return false;
    }

    public void replaceWordAt(int i, String word) {
        if (arrayDoesNotHave(word)) {
            wordsArray.add(i, word);
            words.add(i, word);
        }
    }

    /**
     * Checks whether the given word should be added to wordsArray, by comparing
     * all the entries in wordsArray against the given word, both turned lowercase
     * to make sure there are no duplicates.
     *
     * @param word Word to check against
     * @return Whether wordsArray does not contain the given word
     */
    private boolean arrayDoesNotHave(String word) {
        for (String str : wordsArray)
            if (str.toLowerCase(Locale.ROOT).equals(word.toLowerCase(Locale.ROOT))) return false;

        return true;
    }

    public void editSelectedWord() {

        if (!wordList.isSelectionEmpty()) {
            int index = wordList.getSelectedIndex();
            String newWord = new EditWordDialog(this, words.get(index)).getWord();
            if (!newWord.isEmpty()) {
                removeSelectedWord(false);
                replaceWordAt(index, newWord);
                wordList.setSelectedIndex(index);
            }
        }

    }

    public void setStatusText(String t) {
        statusText.setText(t);
        setSelectedIndex(1); // Switch to definitions tab to show new text
    }

    public String getText() {
        return statusText.getText();
    }

    public String getSelectedText() {
        return statusText.getSelectedText();
    }

    public void clear() {
        wordsArray.clear();
        words.clear();
    }

}
