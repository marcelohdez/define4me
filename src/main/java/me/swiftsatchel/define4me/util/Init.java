package me.swiftsatchel.define4me.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public final class Init {

    /**
     * Initializes the given buttons with a KeyListener and a hand cursor
     * @param buttons Buttons to initialize
     */
    public static void buttons(KeyListener kl, AbstractButton... buttons) {
        for (AbstractButton b : buttons) {
            if (kl != null) b.addKeyListener(kl);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

}
