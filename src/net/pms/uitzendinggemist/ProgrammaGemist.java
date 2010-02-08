/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.pms.uitzendinggemist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 *
 * @author paulwagener
 */
public class ProgrammaGemist extends VirtualFolder {
    public ProgrammaGemist()
    {

        super("SBS", null);
    }

    public static void main(String args[])
    {
        String programmaPagina = HTTPWrapper.Request("http://www.veronicatv.nl/web/show/id=113774/langid=43");

        //Filter op overzicht
        Matcher m1 = Pattern.compile("(?s)(Programma gemist overzicht|Bekijk alle afleveringen).*?class=\"bottom\"").matcher(programmaPagina);
        if(m1.find())
            programmaPagina = m1.group();

        //Zoek shows
        Matcher m = Pattern.compile("(?s)class=\"thumb\".*?href=\"(/web/show.*?)\".*?src=\"(.*?)\".*?<span>(.*?)</span>").matcher(programmaPagina);

        //TODO: iets DOEN met de data
        while (m.find()) {
            System.out.println(m.group(3) + " " + m.group(1) + " " + m.group(2));
        }
    }

}
