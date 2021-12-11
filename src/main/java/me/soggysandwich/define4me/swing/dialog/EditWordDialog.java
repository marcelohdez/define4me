package me.soggysandwich.define4me.swing.dialog;

public class EditWordDialog extends AddWordDialog {

    public EditWordDialog(String word) {
        setTitle("Edit Word");
        textField.setText(word);
    }

}
