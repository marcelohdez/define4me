package me.swiftsatchel.define4me.swing.comp;

import me.swiftsatchel.define4me.swing.AppWindow;
import me.swiftsatchel.define4me.swing.dialog.EditWordDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class MiddlePane extends JTabbedPane {

    private final ArrayList<String> wordsArray = new ArrayList<>(); // Current list of words

    private final JButton removeButton = new JButton("Remove");
    private final JButton addButton = new JButton("Add");
    private final DefaultListModel<String> words = new DefaultListModel<>();
    private final JList<String> wordList = new JList<>(words);
    private final JTextArea statusText = new JTextArea("""
            No words have been defined yet.
            """);

    public MiddlePane(AppWindow app, JPopupMenu rightClickMenu, KeyListener kl) {
        addTab("Words", createListAndButtonsPanel());
        addTab("Definitions", new JScrollPane(statusText));

        initComps(app, rightClickMenu, kl);
    }

    private void initComps(AppWindow app, JPopupMenu rightClickMenu, KeyListener kl) {

        statusText.setEditable(false);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setComponentPopupMenu(rightClickMenu);

        wordList.addKeyListener(kl);
        wordList.setComponentPopupMenu(rightClickMenu);

        app.initButtons(addButton, removeButton);

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

    public int getWordsSize() {
        return wordsArray.size();
    }

    public String getWordAt(int i) {
        return wordsArray.get(i);
    }

    public String[] getList() {
        return wordsArray.toArray(new String[0]);
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
        if (!wordsArray.contains(word)) { // Avoid adding a duplicate
            wordsArray.add(word);
            words.addElement(word);
            return true;
        } else return false;
    }

    public void addAt(int i, String word) {
        if (!wordsArray.contains(word)) { // Avoid adding a duplicate
            wordsArray.add(i, word);
            words.add(i, word);
        }
    }

    public void editSelectedWord() {

        if (!wordList.isSelectionEmpty()) {
            int index = wordList.getSelectedIndex();
            EditWordDialog ewd = new EditWordDialog(words.get(index));
            if (ewd.accepted()) {
                removeSelectedWord(false);
                addAt(index, ewd.getWord());
                wordList.setSelectedIndex(index);
            }
            ewd.dispose();
        }

    }

    public void setStatusText(String t) {
        statusText.setText(t);
    }

    public String getStatusText() {
        return statusText.getText();
    }

    public String getSelectedStatusText() {
        return statusText.getSelectedText();
    }

    public void clear() {
        wordsArray.clear();
        words.clear();
    }

}
