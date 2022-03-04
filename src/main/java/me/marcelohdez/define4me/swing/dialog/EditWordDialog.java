package me.marcelohdez.define4me.swing.dialog;

import java.awt.*;

public class EditWordDialog extends AddWordDialog {

    public EditWordDialog(Component parent, String word) {
        super(parent);
        setTitle("Edit Word");
        textField.setText(word);
    }

}
