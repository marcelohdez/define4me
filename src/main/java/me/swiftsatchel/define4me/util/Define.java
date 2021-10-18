package me.swiftsatchel.define4me.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class Define {

    public static String allWords(ArrayList<String> words) {

        JSONParser parser = new JSONParser();
        StringBuilder sb = new StringBuilder();

        for (String w : words) {

            sb.append(w).append(" -- "); // Always start with word

            try {

                sb.append(getDefinitionOf(parser, w)); // Get definition

            } catch (Exception e) {
                sb.append("No definition found"); // Add this text if unable to get definition
            }

            sb.append("\n"); // Always add new line

        }

        return sb.toString();

    }

    private static String getJSONText(Reader r) throws IOException {

        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = r.read()) != -1) {
            sb.append((char) c);
        }
        return sb.toString();

    }

    private static String getDefinitionOf(JSONParser parser, String w) throws IOException, ParseException {

        InputStream urlStream = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + w).openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlStream, StandardCharsets.UTF_8));
        String jsonText = getJSONText(reader);

        JSONArray jsonArray = (JSONArray) parser.parse(jsonText); // Get all of json
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);  // Get first use of word
        jsonArray = (JSONArray) jsonObject.get("meanings");     // Get array of "meanings"
        jsonObject = (JSONObject) jsonArray.get(0);             // Get first object of "meaning"
        jsonArray = (JSONArray) jsonObject.get("definitions");  // Get array of definitions
        jsonObject = (JSONObject) jsonArray.get(0);             // Get first definition object

        return jsonObject.get("definition").toString();

    }

}
