/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.uitzendinggemist.Uitzending;

/**
 *
 * @author Paul Wagener
 */
abstract public class UitzendingGemistSite {

    public static List<Uitzending> getUitzendingen(String URL) {
        return getUitzendingen(URL, "");
    }

    public static List<Uitzending> getUitzendingen(String url, String post) {
        //Get cookie!
        HTTPWrapper.Request("http://www.uitzendinggemist.nl/");

        List<Uitzending> uitzendingen = new ArrayList<Uitzending>();
        addUitzendingen(url, post, uitzendingen, 1);

        return uitzendingen;
    }

    private static void addUitzendingen(String url, String post, List<Uitzending> uitzendingen, int pgNum) {
        String lijstPagina = HTTPWrapper.Request(url + "&pgNum=" + pgNum, post);
        
        Matcher m = Pattern.compile("<a[^<]*?md5=.*?>([^<]+?)</a>.*?<td.*?>(.*?)</td>.*?<a href=\"http://player.omroep.nl/\\?aflID=(.*?)\"", Pattern.DOTALL).matcher(lijstPagina);

        
        while (m.find()) {
        
            long time = 0;
            try {
                time = new SimpleDateFormat("dd-MM-yyyy").parse(m.group(2)).getTime();
            } catch (ParseException e) {
            }

            uitzendingen.add(new Uitzending(m.group(1), m.group(3), time));
        }

        //Is er een volgende pagina?
        if (lijstPagina.indexOf(">volgende<") != -1) {
            addUitzendingen(url, post, uitzendingen, pgNum + 1);
        }
    }

}
