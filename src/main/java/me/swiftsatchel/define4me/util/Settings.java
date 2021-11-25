package me.swiftsatchel.define4me.util;

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

    public static boolean prefersFirstGrammar() {
        return PREFS.getBoolean("preferFirstGrammar", true);
    }

    public static void setGrammarPreference(boolean preference) {
        PREFS.putBoolean("preferFirstGrammar", preference);
    }

}
