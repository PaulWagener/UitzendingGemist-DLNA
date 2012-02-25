package net.pms.uitzendinggemist.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to streamline the usage of Regular Expressions
 *
 * @author paulwagener
 */
public abstract class Regex {

    /**
     * Return the first matched group in a text
     * 
     * @param pattern
     * @param text
     * @return
     */
    public static String get(String pattern, String text) {
        Matcher m = Pattern.compile(pattern, Pattern.DOTALL).matcher(text);
        if(m.find())
            return m.group(1);
        else
            return null;
    }

    /**
     * Maak een lijst van alle gematchede results om makkelijk doorheen te itereren.
     *
     * @param pattern
     * @param text
     * @return
     */
    public static List<MatchResult> all(String pattern, String text) {
        List<MatchResult> results = new ArrayList<MatchResult>();

        Matcher m = Pattern.compile(pattern, Pattern.DOTALL).matcher(text);
        while (m.find()) {
            results.add(m.toMatchResult());
        }
        return results;
    }
}
