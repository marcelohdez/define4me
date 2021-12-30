package me.soggysandwich.define4me;

import me.soggysandwich.define4me.swing.AppWindow;
import me.soggysandwich.define4me.util.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class Define4Me {

    public static final String VERSION = "1.2-DEV";
    private static boolean isMacOS;

    public static void main(String[] args) {
        // Try to get the system's look and feel and set to it
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try { // If it failed, try to set cross-platform look and feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception i) { i.printStackTrace(); } // Print error if no look and feel is available
        }
        // If we are on macOS, enable use of the menu bar at the top of the screen (if enabled in settings)
        if (System.getProperty("os.name").equals("Mac OS X")) {
            isMacOS = true;
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
            if (!isMacOS && b instanceof JButton) b.setMargin(new Insets(6, 12, 6, 12));
        }
    }

}
