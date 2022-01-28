package me.marcelohdez.define4me.util;

import java.util.prefs.Preferences;

public final class Settings {

    private static final Preferences PREFS = Preferences.userRoot(); // User's preferences

    public static boolean prefersFirstDefinition() {
        // Setting to ask for a choice if there's multiple definitions available for a word
        return PREFS.getBoolean("preferFirstDefinition", true);
    }

    public static void setDefinitionPreference(boolean preference) {
        PREFS.putBoolean("preferFirstDefinition", preference);
    }

    public static boolean acceptsWikipediaSummary() {
        // Setting on whether a wikipedia summary should be used if a definition for the word is not found
        return PREFS.getBoolean("acceptsWikipediaSummary", true);
    }

    public static void setWikipediaSummaryPreference(boolean preference) {
        PREFS.putBoolean("acceptsWikipediaSummary", preference);
    }

    public static boolean prefersMacMenuBar() {
        // Use macOS's menu bar instead of the in-app one
        return PREFS.getBoolean("preferMacMenuBar", true);
    }

    public static void setMacMenuBarPreference(boolean preference) {
        PREFS.putBoolean("preferMacMenuBar", preference);
    }

}
