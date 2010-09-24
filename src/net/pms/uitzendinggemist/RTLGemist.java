package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.HTTPWrapper;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Browse door alle programma's die op rtl.nl staan
 *
 * @author Paul Wagener
 */
public class RTLGemist extends VirtualFolder {

    public RTLGemist() {
        super("RTL", null);
    }

    /**
     * Deze map heeft een generiek RTL logootje.
     */
    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return downloadAndSend("http://rtl.nl/experience/rtlnl/components/images/2007/rtl-logo-family-162x90.jpg", true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }

    /**
     * Zet de lijst van series die op http://www.rtl.nl/service/index/ staan
     * Om in twee folders: Een met alle series gesorteerd op naam en een met alle series gesorteerd op genre.
     */
    @Override
    public void discoverChildren() {
        super.discoverChildren();

        //Deze week
        //Geeft een lijst van afleveringen op: http://www.rtl.nl/service/gemist/home/
        addChild(new VirtualFolder("Deze week", null) {

            @Override
            public void discoverChildren() {
                String pagina = HTTPWrapper.Request("http://www.rtl.nl/service/gemist/home/");
                for (MatchResult m : Regex.all("<div class=\"om\">(.*?)</div><div class=\"op\">(.*?)</div>.*?<a class=\"a_uitzending.*?\" href=\"(.*?)\" title=\"(.*?)\">", pagina)) {
                    String tijd = m.group(1);
                    String zender = m.group(2);
                    String uitzendingUrl = "http://www.rtl.nl" + m.group(3);
                    String titel = StringEscapeUtils.unescapeHtml(m.group(4)) + " (" + tijd + " " + zender + ")";
                    addChild(new RTLUitzending(titel, uitzendingUrl, null, 0));
                }
            }
        });

        //Series op titel
        addChild(new VirtualFolder("Op titel", null) {

            @Override
            public void discoverChildren() {
                Set<RTLSerie> series = getAlleSeries();
                HashMap<Character, VirtualFolder> folders = new HashMap<Character, VirtualFolder>();

                //Maak letter folders (plus een folder voor series die met een cijfer beginnen)
                VirtualFolder numeriek = new VirtualFolder("0-9", null);
                addChild(numeriek);

                String alfabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                for (int i = 0; i < alfabet.length(); i++) {
                    VirtualFolder folder = new VirtualFolder("" + alfabet.charAt(i), null);
                    folders.put(alfabet.charAt(i), folder);
                    addChild(folder);
                }

                //Zet series in de letter folders
                for (RTLSerie serie : series) {
                    char firstLetter = Character.toUpperCase(serie.getName().charAt(0));
                    if (folders.containsKey(firstLetter)) {
                        folders.get(firstLetter).addChild(serie);
                    } else {
                        //Zet restjes in de numerieke folder
                        numeriek.addChild(serie);
                    }
                }
            }
        });

        //Series op genre
        addChild(new VirtualFolder("Op genre", null) {

            @Override
            public void discoverChildren() {
                Set<RTLSerie> series = getAlleSeries();
                HashMap<String, VirtualFolder> folders = new HashMap<String, VirtualFolder>();

                //Zet series in een seriefolder
                for (RTLSerie serie : series) {

                    //Het genre is te vinden tussen de eerste twee slashes van de videomenuUrl.
                    String genre = Regex.get("/(.*?)/", serie.videomenuUrl);

                    //Als genre voor de eerste keer tegenkomt deze toevoegen aan "Op genre" map
                    //En onthouden in tijdelijke 'folders' variabele
                    if (!folders.containsKey(genre)) {
                        VirtualFolder folder = new VirtualFolder(genre, null);
                        folders.put(genre, folder);
                        addChild(folder);
                    }

                    //Voeg serie toe aan juiste genre map
                    folders.get(genre).addChild(serie);
                }
            }
        });
    }
    static Set<RTLSerie> series = null;

    /**
     * Haal lijst op van alle programma's die RTL aanbied uit hun A-Z index
     * Houdt GEEN rekening mee of programma's ook daadwerkelijk gemiste afleveringen hebben...
     */
    static Set<RTLSerie> getAlleSeries() {
        if (series == null) {
            series = new HashSet<RTLSerie>();

            String pagina = HTTPWrapper.Request("http://www.rtl.nl/service/index/");
            for (MatchResult m : Regex.all("<li><a.*?href=\"(.*?)\".*?>(.*?)</a></li>", pagina)) {
                String url = m.group(1);
                String titel = m.group(2);

                series.add(new RTLSerie(titel, url));

            }
        }
        return series;
    }

    /**
     * 
     */
    static class RTLSerie extends VirtualFolder {

        String videomenuUrl;

        /**
         * Maak een serie met een naam
         * en een url in de vorm van: (http://www.rtl.nl)/actueel/rtlnieuws(/home/ || /videomenu.xml)
         * url wordt opgeslagen als: /actueel/rtlnieuws/videomenu.xml
         *
         * Een serie kan ook een subserie zijn, bijvoorbeeld: /actueel/rtlnieuws/video/buitenland/videomenu.xml
         */
        public RTLSerie(String naam, String url) {
            super(naam, null);
            url = url.replace("/home/", "");            //Haal home aan het einde weg
            url = url.replace("http://www.rtl.nl", ""); //Haal domein aan het begin weg
            if (!url.endsWith("xml")) {
                if (!url.endsWith("/")) {
                    url += "/";
                }
                url += "videomenu.xml";
            }

            

            this.videomenuUrl = url;
        }

        /**
         * Zoek bij het openen van deze folder naar video's en subfolders
         */
        @Override
        public void discoverChildren() {
            super.discoverChildren();
            String videomenu = HTTPWrapper.Request("http://www.rtl.nl/system/video/menu" + videomenuUrl);

            //Voeg nieuwe videomenu.xml's in de hierarchie op rtl.nl toe als subfolders.
            //Deze mapjes lijken arbitrair diep te kunnen gaan.
            for (MatchResult m : Regex.all("<li class=\"folder\" rel=\"(.*?)\">(.*?)</li>", videomenu)) {
                String folderUrl = m.group(1);
                String titel = m.group(2);

                addChild(new RTLSerie(titel, folderUrl));
            }

            //Parse de video's die in de videomenu.xml staan
            for (MatchResult m : Regex.all("<li class=\"video\" thumb=\"(.*?)\".*?ctime=\"(.*?)\".*?rel=\"(.*?)\".*?>(.*?)</li>", videomenu)) {
                String img = "http://www.rtl.nl" + m.group(1);
                String uitzendtijd = m.group(2);
                String uitzendingurl = "http://www.rtl.nl" + m.group(3);
                String titel = m.group(4);

                addChild(new RTLUitzending(titel, uitzendingurl, img, Long.parseLong(uitzendtijd)));
            }
        }
    }

    /**
     * Een uitzending met een titel, afbeelding 
     * en een url waar de video normaal is te bekijken
     */
    static class RTLUitzending extends WebStream {

        String uitzendingUrl;
        String mmsUrl = null;

        public RTLUitzending(String titel, String url, String img, long tijd) {
            super(titel, "mms://null/null", img, Format.VIDEO);
            this.uitzendingUrl = url;
            this.lastmodified = tijd;
        }

        /**
         * Zoek als de gebruiker de stream wil bekijken
         * op het laatste moment nog naar de eigenlijke .wmv locatie van de stream
         */
        @Override
        public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
            if (mmsUrl == null) {
                PMS.minimal("RTL Link: " + uitzendingUrl);
                String uitzendingPagina = HTTPWrapper.Request(uitzendingUrl);
                mmsUrl = Regex.get("file:'(.*?)'", uitzendingPagina).replace("http://", "mms://");
            }

            PMS.minimal("RTL Stream: " + mmsUrl);
            this.URL = mmsUrl;
            return super.getInputStream(low, high, timeseek, mediarenderer);
        }
    }

    public static void main(String args[]) {
        //getAlleSeries();
    }
}
