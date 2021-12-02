package me.swiftsatchel.define4me;

import me.swiftsatchel.define4me.swing.AppWindow;
import me.swiftsatchel.define4me.util.Settings;

import javax.swing.*;

public class Main {

    public static final String VERSION = "1.0-DEV";

    public static void main(String[] args) {

        // Try to get the system's look and feel and set to it
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try { // If it failed, try to set cross-platform look and feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception i) { // If no look-and feel is available then just print error:
                System.out.println("Unable to set any look and feel. Stack trace:");
                i.printStackTrace();
            }
        }

        if (System.getProperty("os.name").equals("Mac OS X") && Settings.prefersMacMenuBar())
            System.setProperty("apple.laf.useScreenMenuBar", "true");

        new AppWindow(); // Open app window

    }

}
