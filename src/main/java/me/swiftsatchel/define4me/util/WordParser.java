package me.swiftsatchel.define4me.util;

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
            if (!string.substring(i, i+1).isBlank()) {
                firstLetter = i;
                break;
            }

        // Check through every character
        for (int i = firstLetter; i < string.length(); i++) {
            char c = string.charAt(i);
            // Check if there is a letter after this space, if so add it else end this word here.
            if (Character.isSpaceChar(c)) {
                if (i + 1 < string.length()) // If there is a next character:
                    if (!Character.isLetter(string.charAt(i + 1))) {
                        break;
                    } else wordBuilder.append(c);

            } else if (Character.isLetter(c) || c == '-') // If current character is a letter or hyphen append it:
                wordBuilder.append(c);
        }

        return wordBuilder.toString();

    }

}
