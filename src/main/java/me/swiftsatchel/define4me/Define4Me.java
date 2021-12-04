package me.swiftsatchel.define4me;

import me.swiftsatchel.define4me.swing.AppWindow;
import me.swiftsatchel.define4me.util.Settings;

import javax.swing.*;

public class Define4Me {

    public static final String VERSION = "1.0";

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
        if (System.getProperty("os.name").equals("Mac OS X") && Settings.prefersMacMenuBar())
            System.setProperty("apple.laf.useScreenMenuBar", "true");

        new AppWindow(); // Open app window
    }

}
