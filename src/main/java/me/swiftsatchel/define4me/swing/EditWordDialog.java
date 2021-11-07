package me.swiftsatchel.define4me.swing;

public class EditWordDialog extends AddWordDialog {

    public EditWordDialog(String word) {
        setTitle("Edit Word");
        textField.setText(word);
    }

}
