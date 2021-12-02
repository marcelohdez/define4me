package me.swiftsatchel.define4me.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public final class Init {

    /**
     * Initializes the given buttons with a KeyListener, a hand cursor, and
     * some padding if they are JButtons.
     *
     * @param buttons Buttons to initialize
     */
    public static void buttons(KeyListener kl, AbstractButton... buttons) {
        for (AbstractButton b : buttons) {
            if (kl != null) b.addKeyListener(kl);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (b instanceof JButton) b.setMargin(new Insets(6, 12, 6, 12));
        }
    }

}
