/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import javax.swing.JComponent;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.external.ExternalListener;

/**
 *
 * @author Paul Wagener
 */
public class UitzendingGemist implements ExternalListener {

    private static final String DAG_URL = "http://www.uitzendinggemist.nl/index.php/selectie?searchitem=dag&dag=";
    private static final String TITEL_URL = "http://www.uitzendinggemist.nl/index.php/selectie?searchitem=titel&titel=";
    private static final String SELECTIE_URL = "http://www.uitzendinggemist.nl/index.php/selectie";
    //private static final String

    public UitzendingGemist() {
        VirtualFolder mainFolder = new VirtualFolder("Uitzending Gemist", null);

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
            UitzendingFolder folder = new UitzendingFolder(alfabet.charAt(i) == '#' ? "0-9" : "" + alfabet.charAt(i), TITEL_URL + i, "items=actueel");
            folder.addChild(new UitzendingFolder("Alle programma's", TITEL_URL + i));
            titelFolder.addChild(folder);
        }

        //Op genre
        final String[][] genres = {{"1", "Amusement"}, {"2", "Animatie"}, {"3", "Comedy"}, {"4", "Documentaire"},
            {"21", "Drama"}, {"24", "Educatief"}, {"5", "Erotiek"}, {"6", "Film"},
            {"27", "Gezondheid"}, {"7", "Informatief"}, {"8", "Jeugd"}, {"25", "Kinderen 2-5"},
            {"26", "Kinderen 6-12"}, {"23", "Klassiek"}, {"9", "Kunst/Cultuur"}, {"19", "Maatschappij"},
            {"10", "Misdaad"}, {"11", "Muziek"}, {"12", "Natuur"}, {"13", "Nieuws/actualiteiten"},
            {"14", "Overige"}, {"15", "Religieus"}, {"16", "Serie/soap"}, {"17", "Sport"}, {"18", "Wetenschap"}};

        VirtualFolder genreFolder = new VirtualFolder("Op genre", null);
        for (int i = 0; i < genres.length; i++) {
            final String url = SELECTIE_URL + "?searchitem=genre&genre=" + genres[i][0];
            UitzendingFolder folder = new UitzendingFolder(genres[i][1], url, "items=actueel");
            folder.addChild(new UitzendingFolder("Alle programma's", url));
            genreFolder.addChild(folder);
        }

        //Op omroep
        final String[][] omroepen = {
            {"33", "3FM"}, {"11", "AVRO"}, {"16", "BNN"}, {"7", "BOS"}, {"15", "EO"}, {"29", "HUMAN"}, {"3", "IKON"}, {"48", "Joodse Omroep"}, {"2", "KRO"},
            {"45", "LLiNK"}, {"46", "MAX"}, {"52", "MTNL"}, {"8", "NCRV"}, {"55", "Nederland 1"}, {"56", "Nederland 2"},
            {"21", "Nederland 3"}, {"49", "NIO"}, {"4", "NMO"}, {"12", "NOS"}, {"22", "NPS"}, {"5", "OHM"},
            {"50", "Omroep-nl"}, {"6", "OMROP FRYSLAN"}, {"32", "Radio 2"}, {"27", "Radio 4"}, {"1", "RKK"}, {"25", "RNW"}, {"23", "RVU"}, {"43", "TELEAC"},
            {"14", "TROS"}, {"17", "VARA"}, {"20", "VPRO"}, {"47", "Z@pp"}, {"19", "Z@ppelin"}, {"28", "ZvK"}};

        VirtualFolder omroepFolder = new VirtualFolder("Op omroep", null);
        for (int i = 0; i < omroepen.length; i++) {
            final String url = SELECTIE_URL + "?searchitem=omroep&omroep=" + omroepen[i][0];
            UitzendingFolder folder = new UitzendingFolder(omroepen[i][1], url, "items=actueel");
            folder.addChild(new UitzendingFolder("Alle programma's", url));
            omroepFolder.addChild(folder);
        }

        mainFolder.addChild(dezeweekFolder);
        mainFolder.addChild(titelFolder);
        mainFolder.addChild(genreFolder);
        mainFolder.addChild(omroepFolder);



        //Voeg Uitzending Gemist toe aan alle configuraties
        //TODO: Wachten op PMS om hook toe te voegen voor plugins om aan rootfolders iets toe te voegen
        int i = 1;
        RendererConfiguration conf = RendererConfiguration.getRendererConfiguration(i);
        while (conf != RendererConfiguration.getDefaultConf()) {
            conf.getRootFolder().addChild(mainFolder);

            i++;
            conf = RendererConfiguration.getRendererConfiguration(i);
        }
    }

    public static void main(String args[]) {
        //new UitzendingFolder("Woensdag", TOP50_URL).discoverChildren();
    }

    public JComponent config() {
        return null;
    }

    public String name() {
        return "Uitzending Gemist";
    }

    public void shutdown() {
    }
}
