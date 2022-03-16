package me.marcelohdez.define4me.test;

import me.marcelohdez.define4me.util.WordParser;
import org.junit.Test;

import static org.junit.Assert.*;

public class WordParserTest {
    @Test
    public void removeExtraSpaces() {
        assertEquals("One two three", WordParser.parseString("One    two  three"));
    }

    @Test
    public void removeExtraHyphens() {
        assertEquals("One-two-three", WordParser.parseString("One---two--three"));
    }

    @Test
    public void capitalizeFirstLetter() {
        assertEquals("Word", WordParser.parseString("word"));
    }

    @Test
    public void trimBeforeAndAfterWord() {
        assertEquals("Hello", WordParser.parseString("?//-  hello--!"));
    }

    @Test
    public void returnBlankIfNoLetters() {
        assertEquals("", WordParser.parseString("!-'12345 "));
    }
}
