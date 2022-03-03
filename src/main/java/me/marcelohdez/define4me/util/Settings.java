package me.marcelohdez.define4me.util;

import java.util.prefs.Preferences;

public final class Settings {

    private static final Preferences PREFS = Preferences.userRoot(); // User's preferences

    // Keys
    private static final String KEY_DEFINITION_PREF = "preferFirstDefinition";
    private static final String KEY_WIKI_PREF = "wikipediaPreference";
    private static final String KEY_MENU_BAR_PREF = "preferMacMenuBar";

    // Wikipedia preference options:
    public static final int WIKI_PREF_NEVER = 0;
    public static final int WIKI_PREF_AS_BACKUP = 1;
    public static final int WIKI_PREF_ALWAYS = 2;

    // Getters
    public static boolean prefersFirstDefinition() {
        // Setting to ask for a choice if there's multiple definitions available for a word
        return PREFS.getBoolean(KEY_DEFINITION_PREF, true);
    }

    public static int wikiPreference() {
        // Setting on whether a wikipedia summary should be used if a definition for the word is not found
        return PREFS.getInt(KEY_WIKI_PREF, WIKI_PREF_AS_BACKUP);
    }

    public static boolean prefersMacMenuBar() {
        // Use macOS's menu bar instead of the in-app one
        return PREFS.getBoolean(KEY_MENU_BAR_PREF, true);
    }

    // Setters
    public static void setDefinitionPreference(boolean preference) {
        PREFS.putBoolean(KEY_DEFINITION_PREF, preference);
    }

    public static void setWikiPreference(int preference) {
        PREFS.putInt(KEY_WIKI_PREF, preference);
    }

    public static void setMacMenuBarPreference(boolean preference) {
        PREFS.putBoolean(KEY_MENU_BAR_PREF, preference);
    }

}
