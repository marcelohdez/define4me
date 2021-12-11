package me.soggysandwich.define4me.util;

public final class WordParser {

    /**
     * Parses the given string for its word
     *
     * @param string Given string, preferably a single line
     * @return The parsed letters, hyphens, or spaces
     */
    public static String parseString(String string) {
        StringBuilder wordBuilder = new StringBuilder();
        // Find starting letter to not keep any starting space, ex: "  hi" -> "hi"
        int firstLetter = 0;
        for (int i = 0; i < string.length(); i++)
            if (Character.isLetter(string.charAt(i))) {
                firstLetter = i;
                break;
            }

        // Check through every character
        int lastLetter = firstLetter;
        for (int i = firstLetter; i < string.length(); i++) {
            if (string.startsWith(" -", i)) break; // For lists with hyphens after words
            char c = string.charAt(i); // Save current char
            // If character is a space and there's no letter afterwards continue, else add it.
            if (Character.isSpaceChar(c)) {
                if (i + 1 < string.length()) {
                    if (!Character.isLetter(string.charAt(i + 1))) continue;
                    wordBuilder.append(c);
                }
            } else if (Character.isLetter(c)) { // Else if it's a letter add it and set lastLetter to next index
                wordBuilder.append(c);
                lastLetter = i + 1;
            } else if (c == '-') wordBuilder.append(c); // Else if it's a hyphen just add it.
        }
        // Remove stuff after last letter:
        if (wordBuilder.length() > lastLetter) wordBuilder.delete(lastLetter, wordBuilder.length());

        return wordBuilder.toString();
    }

}
