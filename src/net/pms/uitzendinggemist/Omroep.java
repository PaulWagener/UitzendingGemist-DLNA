/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 *
 * @author Paul Wagener
 */
public class Omroep extends VirtualFolder {

    private static final String DAG_URL = "http://www.uitzendinggemist.nl/index.php/selectie?searchitem=dag&dag=";
    private static final String TITEL_URL = "http://www.uitzendinggemist.nl/index.php/selectie?searchitem=titel&titel=";
    private static final String SELECTIE_URL = "http://www.uitzendinggemist.nl/index.php/selectie";

    public Omroep() {
        super("Publieke Omroep", null);
    }

    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return this.downloadAndSend("http://assets.www.omroep.nl/images/footer-logo.png", true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }

    
    @Override
    public void discoverChildren() {
        super.discoverChildren();

        //Deze week
        VirtualFolder dezeweekFolder = new VirtualFolder("Deze week", null);
        dezeweekFolder.addChild(new UitzendingFolder("Vandaag", DAG_URL + "vandaag"));
        dezeweekFolder.addChild(new UitzendingFolder("Gisteren", DAG_URL + "gisteren"));
        dezeweekFolder.addChild(new UitzendingFolder("Maandag", DAG_URL + "1"));
        dezeweekFolder.addChild(new UitzendingFolder("Dinsdag", DAG_URL + "2"));
        dezeweekFolder.addChild(new UitzendingFolder("Woensdag", DAG_URL + "3"));
        dezeweekFolder.addChild(new UitzendingFolder("Donderdag", DAG_URL + "4"));
        dezeweekFolder.addChild(new UitzendingFolder("Vrijdag", DAG_URL + "5"));
        dezeweekFolder.addChild(new UitzendingFolder("Zaterdag", DAG_URL + "6"));
        dezeweekFolder.addChild(new UitzendingFolder("Zondag", DAG_URL + "7"));

        //Op titel
        VirtualFolder titelFolder = new VirtualFolder("Op titel", null);

        String alfabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < alfabet.length(); i++) {
            UitzendingFolder folder = new UitzendingFolder(alfabet.charAt(i) == '#' ? "0-9" : ""+alfabet.charAt(i), TITEL_URL + i, "items=actueel");
            folder.addChild(new UitzendingFolder("Alle programma's", TITEL_URL + i));
            titelFolder.addChild(folder);
        }

        String homePagina = HTTPWrapper.Request("http://www.uitzendinggemist.nl/");
 
        
        //Op genre
        
        String genres = Regex.get("name=\"genre\"(.*?)</select>", homePagina);
        
        VirtualFolder genreFolder = new VirtualFolder("Op genre", null);
        
        for(MatchResult m : Regex.all("<option value=\"(.*?)\".*?>(.*?)</option>", genres)) {
            if(m.group(1).equals(""))
                continue;
            
            final String url = SELECTIE_URL + "?searchitem=genre&genre=" + m.group(1);
            UitzendingFolder folder = new UitzendingFolder(m.group(2), url, "items=actueel");
            folder.addChild(new UitzendingFolder("Alle programma's", url));
            genreFolder.addChild(folder);
        }
        
        //Op omroep
        String omroepen = Regex.get("name=\"omroep\"(.*?)</select>", homePagina);
        
        VirtualFolder omroepFolder = new VirtualFolder("Op omroep", null);
        for(MatchResult m : Regex.all("<option value=\"(.*?)\".*?>(.*?)</option>", omroepen))
        {
            if(m.group(1).equals(""))
                continue;

            final String logo = "http://u.uitzendinggemist.nl/pics/omroepen/" + m.group(2).replaceAll(" ", "").replaceAll("@", "a").toLowerCase() + "-logo.jpg";
            final String url = SELECTIE_URL + "?searchitem=omroep&omroep=" + m.group(1);
            
            UitzendingFolder folder = new UitzendingFolder(m.group(2), url, "items=actueel", logo);
            folder.addChild(new UitzendingFolder("Alle programma's", url));
            omroepFolder.addChild(folder);
        }

        addChild(dezeweekFolder);
        addChild(titelFolder);
        addChild(genreFolder);
        addChild(omroepFolder);
        addChild(new Nederland24Folder());
    }

    public static void main(String args[]) {
        new Omroep().discoverChildren();
    }
}
