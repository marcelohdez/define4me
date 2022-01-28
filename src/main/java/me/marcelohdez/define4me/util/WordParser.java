package me.marcelohdez.define4me.util;

public final class WordParser {

    /**
     * Trims the given string and removes any extra spacing between words,
     * then returns the remaining letters, spaces, and hyphens.
     *
     * @param string Given string, preferably a single line
     * @return The parsed letters, hyphens, or spaces
     */
    public static String parseString(String string) {
        StringBuilder wordBuilder = new StringBuilder();
        string = string.trim();

        int lastLetter = 0; // Keep track of final letter to remove non-space characters ahead of it
        // Check through every character
        for (int i = 0; i < string.length(); i++) {
            if (string.startsWith(" -", i)) break; // For lists with hyphens at the end of words
            char c = string.charAt(i); // Save current char

            // If character is a space or hyphen and there's no character afterwards then skip
            if (Character.isSpaceChar(c) || c == '-') {
                if (i + 1 < string.length()) {
                    if (!Character.isLetter(string.charAt(i + 1))) continue;
                    wordBuilder.append(c);
                }
            } else if (Character.isLetter(c)) { // Else if it's a letter add it
                wordBuilder.append(c);
                lastLetter = i;
            }
        }

        if (wordBuilder.length() > lastLetter) {
            return wordBuilder.substring(0, lastLetter + 1);
        } else return wordBuilder.toString();
    }

}
