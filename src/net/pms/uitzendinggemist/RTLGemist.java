package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.HTTPWrapper;
import net.pms.uitzendinggemist.web.XmlFile;

/**
 *
 * @author Job
 */
public class RTLGemist extends VirtualFolder {

    public RTLGemist() {
        super("RTL", null);
    }

    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return downloadAndSend("http://rtl.nl/experience/rtlnl/components/images/2007/rtl-logo-family-162x90.jpg", true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        //addChild(new VirtualFolder("Laatst toegevoegd", null));
        //addChild(new VirtualFolder("Deze week", null));


        //Series op titel
        addChild(new VirtualFolder("Op titel", null) {
            @Override
            public void discoverChildren() {
                Set<RTLSerie> series = getAlleSeries();
                HashMap<Character, VirtualFolder> folders = new HashMap<Character, VirtualFolder>();
                
                //Maak letter folders
                VirtualFolder numeriek = new VirtualFolder("0-9", null);
                addChild(numeriek);
                
                String alfabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                for (int i = 0; i < alfabet.length(); i++) {
                    VirtualFolder folder = new VirtualFolder(""+alfabet.charAt(i), null);
                    addChild(folder);
                    folders.put(alfabet.charAt(i), folder);
                }

                //Zet series in letter folders
                for(RTLSerie serie : series) {
                    char firstLetter = Character.toUpperCase(serie.getName().charAt(0));
                    if(folders.containsKey(firstLetter))
                        folders.get(firstLetter).addChild(serie);
                    else
                        numeriek.addChild(serie);
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
                for(RTLSerie serie : series) {
                    String genre = Regex.get("/(.*?)/", serie.url);
                    if(!folders.containsKey(genre)) {
                        VirtualFolder folder = new VirtualFolder(genre, null);
                        addChild(folder);
                        folders.put(genre, folder);
                    }

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
    static Set<RTLSerie> getAlleSeries()
    {
        if(series == null) {
            series = new HashSet<RTLSerie>();

            String pagina = HTTPWrapper.Request("http://www.rtl.nl/service/index/");
            for(MatchResult m : Regex.all("<li><a.*?href=\"(.*?)\".*?>(.*?)</a></li>", pagina))
            {
                String url = m.group(1);
                String titel = m.group(2);

                series.add(new RTLSerie(titel, url));
                
            }
        }
        PMS.minimal("count: " + series.size());
        return series;
    }

    static class RTLSerie extends VirtualFolder {
        String url;

        public RTLSerie(String naam, String url) {
            super(naam, null);
            url = url.replace("/home/", "");
            url = url.replace("http://www.rtl.nl", "");
            if(!url.endsWith("xml"))
                url = url + "/videomenu.xml";

            this.url = url;
        }

        @Override
        public void discoverChildren() {
            super.discoverChildren();
            String videomenu = HTTPWrapper.Request("http://www.rtl.nl/system/video/menu" + url);

            //Subfolders
            for(MatchResult m : Regex.all("<li class=\"folder\" rel=\"(.*?)\">(.*?)</li>", videomenu)) {
                String folderUrl = m.group(1);
                String titel = m.group(2);

                addChild(new RTLSerie(titel, folderUrl));
            }

            //Video's
            for(MatchResult m : Regex.all("<li class=\"video\" thumb=\"(.*?)\".*?ctime=\"(.*?)\".*?rel=\"(.*?)\".*?>(.*?)</li>", videomenu)) {
                String img = "http://www.rtl.nl" + m.group(1);
                String uitzendtijd = m.group(2);
                String uitzendingurl = "http://www.rtl.nl" + m.group(3);
                String titel = m.group(4);

                addChild(new RTLUitzending(titel, uitzendingurl, img, Long.parseLong(uitzendtijd)));
            }
        }
    }

    static class RTLUitzending extends WebStream {
        String uitzendingUrl;
        String mmsUrl = null;

        public RTLUitzending(String titel, String url, String img, long tijd) {
            super(titel, "mms://null/null", img, Format.VIDEO);
            this.uitzendingUrl = url;
            this.lastmodified = tijd;
        }

        @Override
        public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
            if(mmsUrl == null) {
                String uitzendingPagina = HTTPWrapper.Request(uitzendingUrl);
                mmsUrl = Regex.get("file: '(.*?)'", uitzendingPagina).replace("http://", "mms://");
            }

            PMS.minimal("RTL Stream: " + mmsUrl);
            this.URL = mmsUrl;
            return super.getInputStream(low, high, timeseek, mediarenderer);
        }

        

    }

    public static void main(String args[]) {
        getAlleSeries();
    }
}

