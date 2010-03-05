/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.MatchResult;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.AsxFile;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 *
 * @author paulwagener
 */
public class ProgrammaGemist extends VirtualFolder {

    public enum Site {
        SBS6("http://www.sbs6.nl", "/web/show/id=73863/langid=43", "/design/channel/sbs6/pix/global/header/g-logo.gif"),
        VERONICA("http://www.veronicatv.nl", "/web/show/id=96520/langid=43", "/design/channel/veronicatv/pix/global/header/g-logo.gif"),
        NET5("http://www.net5.nl", "/web/show/id=95681/langid=43", "/design/channel/net5/pix/global/header/g-logo.gif");
        public String base;
        public String startUrl;
        public String logo;

        public String getStartUrl() {
            return base + startUrl;
        }

        public String getLogo() {
            return base + logo;
        }

        Site(String base, String startUrl, String logo) {
            this.base = base;
            this.startUrl = startUrl;
            this.logo = logo;
        }
    }
    
    Site site;

    public ProgrammaGemist(String naam, Site site) {
        super(naam, null);
        this.site = site;
    }


    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return downloadAndSend(site.getLogo(), true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }

    /**
     * Zoek de programma's op dit kanaal
     */
    @Override
    public void discoverChildren() {
        super.discoverChildren();

            String pagina = HTTPWrapper.Request(site.getStartUrl());
            //System.out.println(pagina);
            pagina = Regex.get("<div class=\"mo-a alphabetical\">(.*?)<div class=\"bottom\"></div>", pagina);

            for (MatchResult mf : Regex.all("<a.*?href=\"(/web/show/id=.*?/langid=43)\".*?>(.*?)</a>", pagina)) {
                addChild(new Serie(mf.group(2), site.base + mf.group(1), site));
            }
    }

    public static void main(String args[])
    {
        //new Serie("", "http://www.veronicatv.nl/web/show/id=379041/langid=43").discoverChildren();
    }

    /**
     *
     */
    static class Serie extends VirtualFolder {

        String url;
        Site site;
        public Serie(String naam, String url, Site site) {
            super(naam, null);
            this.url = url;
            this.site = site;
        }

        @Override
        public void discoverChildren() {
            super.discoverChildren();
            
            String pagina = HTTPWrapper.Request(url);

            pagina = Regex.get("<div class=\"mo-c(.*?)<div class=\"bottom\"></div>", pagina);

            for(MatchResult m : Regex.all("<div class=\"item.*?<img src=\"(.*?)\".*?href=\"(/web/show/id=.*?)\".*?><span>(.*?)</span>", pagina)) {
                addChild(new ProgrammaStream(m.group(3), this.site.base + m.group(2), this.site.base + m.group(1)));
            }
        }
    }

    //Stream die vlak voor afspelen de .URL goed zet
    public static class ProgrammaStream extends WebStream {

        String locatieUrl; //Webpagina waar filmpje te bekijken is
        String mms = null; //mms stream

        public ProgrammaStream(String naam, String url, String img) {
            super(naam, "mms://url.url/url", img, Format.VIDEO);
            this.locatieUrl = url;
        }

        @Override
        public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
            if (mms == null) {
                String pagina = HTTPWrapper.Request(locatieUrl);

                String wmvFile = Regex.get("(?s)class=\"wmv-player-holder\" href=\"(.*?)\"", pagina);
                
                //WMV file is altijd maar een verwijzing naar mms:// url die er in zit
                this.URL = mms = new AsxFile(wmvFile).getMediaStream();
                PMS.minimal("Mediastream: " + URL);
            }

            return super.getInputStream(low, high, timeseek, mediarenderer);
        }
    }
}
