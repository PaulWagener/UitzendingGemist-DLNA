/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import net.pms.uitzendinggemist.web.Regex;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.MatchResult;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.Range;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.AsxFile;
import net.pms.uitzendinggemist.web.HTTPWrapper;
import net.pms.uitzendinggemist.web.Util;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Wagener
 */
public class PubliekeOmroep extends VirtualFolder {

    public PubliekeOmroep() {
        super("Publieke Omroep", null);
    }

    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return this.downloadAndSend("http://assets.www.publiekeomroep.nl/images/footer-logo.png", true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();


        VirtualFolder dezeweekFolder = new VirtualFolder("Deze week", null);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

        // Week dagen
        Calendar c = Calendar.getInstance();
        dezeweekFolder.addChild(new AfleveringenFolder("Vandaag", "/weekarchief/" + f.format(c.getTime()), null));
        c.add(Calendar.DAY_OF_YEAR, -1);
        dezeweekFolder.addChild(new AfleveringenFolder("Gisteren", "/weekarchief/" + f.format(c.getTime()), null));
        for (int i = 0; i < 5; i++) {
            c.add(Calendar.DAY_OF_YEAR, -1);
            String dagNaam = null;
            switch (c.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SATURDAY:
                    dagNaam = "Zaterdag";
                    break;

                case Calendar.SUNDAY:
                    dagNaam = "Zondag";
                    break;

                case Calendar.MONDAY:
                    dagNaam = "Maandag";
                    break;

                case Calendar.TUESDAY:
                    dagNaam = "Dinsdag";
                    break;

                case Calendar.WEDNESDAY:
                    dagNaam = "Woensdag";
                    break;

                case Calendar.THURSDAY:
                    dagNaam = "Donderdag";
                    break;

                case Calendar.FRIDAY:
                default:
                    dagNaam = "Vrijdag";
                    break;
            }
            dezeweekFolder.addChild(new AfleveringenFolder(dagNaam, "/weekarchief/" + f.format(c.getTime()), null));
        }

        //Op titel
        VirtualFolder titelFolder = new VirtualFolder("Op titel", null);

        String alfabet = "#abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < alfabet.length(); i++) {
            String url = "/programmas/" + (alfabet.charAt(i) == '#' ? "0-9" : "" + alfabet.charAt(i)) + "?display_mode=detail";
            titelFolder.addChild(new ProgrammasFolder(alfabet.charAt(i) + "", url));
        }
        
        addChild(dezeweekFolder);
        addChild(titelFolder);
        addChild(new GenresFolder());
        addChild(new OmroepFolder());
        addChild(new Nederland24Folder());
    }

    class GenresFolder extends VirtualFolder {

        public GenresFolder() {
            super("Op genre", null);
        }

        @Override
        public void discoverChildren() {
            super.discoverChildren();
            HTTPWrapper.strCookies = "site_cookie_consent=yes";
            for (MatchResult m : Regex.all("<a href=\"([^\"]*?)\" class=\"genre\" title=\"(.*?)\"", HTTPWrapper.Request("http://www.uitzendinggemist.nl/genres"))) {
                addChild(new ProgrammasFolder(m.group(2), m.group(1)));
            }
        }
    }
    
    class OmroepFolder extends VirtualFolder {

        public OmroepFolder() {
            super("Op omroep", null);
        }

        @Override
        public void discoverChildren() {
            super.discoverChildren();
            HTTPWrapper.strCookies = "site_cookie_consent=yes";
            for (MatchResult m : Regex.all("<a href=\"([^\"]*?)\" class=\"broadcaster\" title=\"(.*?)\">", HTTPWrapper.Request("http://www.uitzendinggemist.nl/omroepen"))) {
                addChild(new ProgrammasFolder(m.group(2), m.group(1)));
            }
            
        }
    }

    /**
     * Een lijst van programmas
     */
    class ProgrammasFolder extends VirtualFolder {

        String url;

        public ProgrammasFolder(String name, String url) {
            super(name, null);
            this.url = url;
        }

        @Override
        public void discoverChildren() {
            super.discoverChildren();

            String nextUrl;
            do {
            	HTTPWrapper.strCookies = "site_cookie_consent=yes";
                String content = HTTPWrapper.Request("http://www.uitzendinggemist.nl" + url);
                for (MatchResult m : Regex.all("<li class=\"series.*?<a href=\"(/programmas/.*?)\".*?title=\"(.*?)\".*?(&quot;(.*?)&quot)?", content)) {
                    addChild(new AfleveringenFolder(m.group(2), m.group(1), m.group(3)));
                }
                nextUrl = Regex.get("rel=\"next\" href=\"(.*?)\"", content);
            } while ((url = nextUrl) != null);
        }
    }

    /**
     * Een lijst van afspeelbare afleveringen
     */
    class AfleveringenFolder extends VirtualFolder {

        String url;
        String img;

        AfleveringenFolder(String naam, String url, String img) {
            super(naam, null);
            this.url = url;
            this.img = img;
        }

        @Override
        public InputStream getThumbnailInputStream() {
            try {
                if (img != null) {
                    return downloadAndSend(img, true);
                }
            } catch (IOException ex) {
            }
            return super.getThumbnailInputStream();
        }

        @Override
        public void discoverChildren() {
            String nextUrl;
            do {
            	HTTPWrapper.strCookies = "site_cookie_consent=yes";
                String content = HTTPWrapper.Request("http://www.uitzendinggemist.nl" + url);
                for (MatchResult m : Regex.all("<a href=\"/afleveringen/([0-9]+)\"[^>]*?><img.*?&quot;(.*?)&quot;.*?<a.*?>(.*?)<", content)) {
                    addChild(new Aflevering(m.group(1), m.group(3), m.group(2)));
                }
                nextUrl = Regex.get("rel=\"next\" href=\"(.*?)\"", content);
            } while ((url = nextUrl) != null);
        }
    }

    /**
     * Een enkele WebStream aflevering op Uitzending Gemist
     */
    class Aflevering extends WebStream {

        String aflevering;
        String imgUrl;

        Aflevering(String aflevering, String naam, String img) {
            super(naam, "mms://url.url/url", img, Format.VIDEO);
            this.aflevering = aflevering;
            this.imgUrl = img;
        }

        /**
         * Subvert Uitzending Gemist en haal stream op aan de hand van een afleveringsnummer
         */
        @Override
        public InputStream getInputStream(Range range, RendererConfiguration mediarenderer) throws IOException {

            String afleveringUrl = "http://www.uitzendinggemist.nl/afleveringen/" + aflevering;

            HTTPWrapper.strCookies = "site_cookie_consent=yes";
            String aflId = Regex.get("data-episode-id=\"([0-9]*?)\"", HTTPWrapper.Request(afleveringUrl));
            String encodedKey = Regex.get("<key>(.*?)</key>", HTTPWrapper.Request("http://pi.omroep.nl/info/security/"));
            String key = new String(Util.base64decode(encodedKey));
            String hashPart = key.split("\\|")[1];
            String hash = Util.md5(aflId + "|" + hashPart);

            String streamContent = HTTPWrapper.Request("http://pi.omroep.nl/info/stream/aflevering/" + aflId + "/" + hash);
            String streamUrl = Regex.get("<stream compressie_formaat=\"wvc1\" compressie_kwaliteit=\"std\">.*?<streamurl>(.*?)</streamurl>", streamContent);
            if (streamUrl == null) {
                streamUrl = Regex.get("<stream compressie_formaat=\"wmv\" compressie_kwaliteit=\"bb\">.*?<streamurl>(.*?)</streamurl>", streamContent);
            }
            streamUrl = streamUrl.trim();


            this.url = new AsxFile(streamUrl).getMediaStream();

            LoggerFactory.getLogger(PMS.class).info("Stream " + this.getName() + ": " + this.url);

            return super.getInputStream(range, mediarenderer);
        }
    }

    public static void main(String args[]) {
    }
}
