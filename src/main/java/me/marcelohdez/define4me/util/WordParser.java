package me.marcelohdez.define4me.util;

import me.marcelohdez.define4me.Define4Me;

public final class WordParser {

    /**
     * Gets the trimmed letters and the given allowed characters from a string.
     *
     * @param string Given string, preferably a single line
     * @return The parsed letters, hyphens, spaces, or other allowed characters
     */
    public static String parseString(String string) {
        StringBuilder wordBuilder = new StringBuilder();

        boolean letterFound = false;
        // Check through every character
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i); // Current char

            // Non-letter allowed characters are only added it if there's been a letter found and a letter after them
            if (isCharacterAllowed(c)) {
                if (letterFound && i + 1 < string.length() && Character.isLetter(string.charAt(i + 1))) {
                    wordBuilder.append(c);
                }
            } else if (Character.isLetter(c)) { // Else if it's a letter add it
                if (letterFound) { // If a letter has been found before, just add this one
                    wordBuilder.append(c);
                } else { // Else, this is the first letter, therefore capitalize it
                    letterFound = true;
                    wordBuilder.append(Character.toUpperCase(c));
                }
            }
        }

        return wordBuilder.toString(); // Return the wordBuilder starting at its first letter
    }

    private static boolean isCharacterAllowed(char c) {
        for (char allowed : Define4Me.ALLOWED_CHARS) {
            if (allowed == c) return true;
        }

        return false;
    }

}
