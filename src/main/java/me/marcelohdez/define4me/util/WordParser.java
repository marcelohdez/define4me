package me.marcelohdez.define4me.util;

import me.marcelohdez.define4me.Define4Me;

import java.util.Locale;

public final class WordParser {

    /**
     * Gets the trimmed letters and the given allowed characters from a string.
     *
     * @param string Given string, preferably a single line
     * @return The parsed letters, hyphens, or spaces
     */
    public static String parseString(String string) {
        string = string.trim();
        string = getLettersFrom(string);

        // Capitalize first letter
        if (!string.isEmpty()) {
            return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
        } else return string;
    }

    /**
     * Goes through the string removing extra spaces and hyphens and returns
     * the remaining letters, spaces, and or hyphens
     */
    private static String getLettersFrom(String string) {
        StringBuilder wordBuilder = new StringBuilder();

        int firstLetter = Integer.MAX_VALUE;
        // Check through every character
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i); // Save current char

            // If character is a space or other non-letter allowed character and there's a letter after it, add it
            if (Character.isSpaceChar(c) || isCharacterAllowed(c)) {
                if (i + 1 < string.length()) {
                    if (Character.isLetter(string.charAt(i + 1))) wordBuilder.append(c);
                }
            } else if (Character.isLetter(c)) { // Else if it's a letter add it
                if (i < firstLetter) firstLetter = i;
                wordBuilder.append(c);
            }
        }

        if (firstLetter == 0) firstLetter++;
        return wordBuilder.substring(firstLetter - 1); // Return the wordBuilder starting at its first letter
    }

    private static boolean isCharacterAllowed(char c) {
        for (char allowed : Define4Me.ALLOWED_CHARS) {
            if (allowed == c) return true;
        }

        return false;
    }

}
