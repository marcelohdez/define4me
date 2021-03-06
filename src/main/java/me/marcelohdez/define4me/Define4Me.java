package me.marcelohdez.define4me;

import me.marcelohdez.define4me.swing.AppWindow;
import me.marcelohdez.define4me.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class Define4Me {

    public static final String VERSION = "1.2-DEV";
    // Characters to allow in words, these are in addition to regular letters:
    public static final char[] ALLOWED_CHARS = {' ', '-', '\''}; // space, hyphen, and apostrophe

    private static boolean isOnMacOS;
    private static final Insets padding = new Insets(6, 12, 6, 12); // Won't be used on macOS

    public static void main(String[] args) {
        // Set look and feel:
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace(); // Print error if no look and feel is available
        }
        // If we are on macOS, enable use of the menu bar at the top of the screen (if enabled in settings)
        if (System.getProperty("os.name").equals("Mac OS X")) {
            isOnMacOS = true;
            if (Settings.prefersMacMenuBar()) System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        new AppWindow(); // Open app window
    }

    /**
     * Gives the given Component(s) a hand cursor, a key listener, and some padding
     * if they are a JButton.
     *
     * @param comps Components to do stuff to
     */
    public static void addHandCursorAndKLTo(KeyListener kl, Component... comps) {
        for (Component c : comps) {
            c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (kl != null) c.addKeyListener(kl);

            if (!isOnMacOS && c instanceof JButton) {
                ((JButton) c).setMargin(padding); // padding will be null if on macOS
            }
        }
    }

    public static boolean isOnMacOS() {
        return isOnMacOS;
    }

}
