package me.soggysandwich.define4me;

import me.soggysandwich.define4me.swing.AppWindow;
import me.soggysandwich.define4me.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class Define4Me {

    public static final String VERSION = "1.2-DEV";
    private static boolean isOnMacOS;

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
     * Initializes the given buttons with a KeyListener, a hand cursor, and
     * some padding if they are JButtons.
     *
     * @param buttons Buttons to initialize
     */
    public static void initButtons(KeyListener kl, AbstractButton... buttons) {
        for (AbstractButton b : buttons) {
            if (kl != null) b.addKeyListener(kl);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (!isOnMacOS && b instanceof JButton) b.setMargin(new Insets(6, 12, 6, 12));
        }
    }

    public static boolean isOnMacOS() {
        return isOnMacOS;
    }

}
