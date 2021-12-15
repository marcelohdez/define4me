package me.soggysandwich.define4me.swing.dialog;

import me.soggysandwich.define4me.Define4Me;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A dialog which allows the user to choose wanted definitions from the given
 * definitions for each word also given
 */
public class DefinitionsDialog extends JDialog {

    private final Component parent;
    private final JPanel definitionsPanel = new JPanel();

    // Hash maps
    private final HashMap<String, ArrayList<String>> definitionArrays = new HashMap<>();
    private final HashMap<String, String> wanted = new HashMap<>();

    public DefinitionsDialog(Component parent) {
        this.parent = parent;

        setTitle("Select desired definition(s)");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        definitionsPanel.setLayout(new BoxLayout(definitionsPanel, BoxLayout.Y_AXIS));

        JButton doneButton = new JButton("Done");
        Define4Me.initButtons(null, doneButton);
        doneButton.addActionListener((e) -> {
            setVisible(false);
            dispose();
        });

        JScrollPane definitionsScrollPane = new JScrollPane(definitionsPanel);
        definitionsScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        add(definitionsScrollPane, BorderLayout.CENTER);
        add(doneButton, BorderLayout.SOUTH);

    }

    public void addMeaningsArrayFor(String word, JSONArray meaningsArray) {
        ArrayList<String> availableDefinitions = new ArrayList<>();

        for (Object value : meaningsArray) { // Look through available meanings
            JSONObject jsonObject = (JSONObject) value;
            JSONArray jsonArray = (JSONArray) jsonObject.get("definitions");  // Get array of definitions
            for (Object o : jsonArray) { // look through definitions
                jsonObject = (JSONObject) o; // Get definition object
                availableDefinitions.add(jsonObject.get("definition").toString());
            }
        }

        definitionArrays.put(word, availableDefinitions);
    }

    /**
     * @return The amount of words this dialog will show to choose definitions from
     */
    public int wordsToShow() {
        return definitionArrays.size();
    }

    /**
     * Shows this dialog and allows user to select desired definitions, once they
     * select "done" it will return the chose definitions in a HashMap organized as
     * (Word, Chosen definition)
     *
     * @return The chosen definitions in a HashMap
     */
    public HashMap<String, String> getWantedDefinitions() {

        for (String word : definitionArrays.keySet())
            definitionsPanel.add(createRowFor(word)); // Add a new page for each word

        pack();
        if (definitionArrays.size() > 10) setSize(new Dimension((int) (getWidth() * 1.1), getWidth()));
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(parent);
        setVisible(true);

        return wanted;

    }

    /**
     * @param word The word we are choosing the definitions for in this page
     * @return A JPanel containing the definition choices for given word
     */
    private JPanel createRowFor(String word) {

        JPanel pnl = new JPanel(new BorderLayout());
        JComboBox<String> definitionsList = new JComboBox<>();
        // Do stuff when item is changed
        definitionsList.addItemListener((e) -> wanted.put(word,
                useDefinitionAt(definitionsList.getSelectedIndex(), word)));

        JLabel wordLabel = new JLabel(word);
        wordLabel.setBorder(new EmptyBorder(6, 6, 6, 12));

        pnl.add(wordLabel, BorderLayout.WEST); // Place word label on left
        pnl.add(definitionsList, BorderLayout.CENTER); // Place definitions list box on center

        for (String def : definitionArrays.get(word)) {
            final int CHAR_LIMIT = 50;
            if (def.length() > CHAR_LIMIT) def = def.substring(0, CHAR_LIMIT - 3) + "...";
            definitionsList.addItem(def);
        }

        return pnl;

    }

    /**
     * @param index Index of definition
     * @param of Word to get definition from
     * @return The definition at the given index of a word's possible definitions
     */
    private String useDefinitionAt(int index, String of) {
        return definitionArrays.get(of).get(index);
    }

}
