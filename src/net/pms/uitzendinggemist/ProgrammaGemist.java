/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.network.HTTPResource;
import net.pms.uitzendinggemist.web.AsxFile;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 *
 * @author paulwagener
 */
public class ProgrammaGemist extends VirtualFolder {

    public enum Site {
        SBS6 ("http://www.sbs6.nl", "/web/show/id=73863/langid=43", "/design/channel/sbs6/pix/global/header/g-logo.gif"),
        VERONICA ("http://www.veronicatv.nl", "/web/show/id=96520/langid=43", "/design/channel/veronicatv/pix/global/header/g-logo.gif");
        public String base;
        public String startUrl;
        public String logo;

        public String getStartUrl()
        {
            return base + startUrl;
        }

        public String getLogo()
        {
            return base + logo;
        }

        Site(String base, String startUrl, String logo) {
            this.base = base;
            this.startUrl = startUrl;
            this.logo = logo;
        }

    }

    Site site;
    boolean losseAfleveringen;
    String url;
    String imgUrl;
    
    public ProgrammaGemist(String naam, Site site) {
        this(naam, site, site.getStartUrl(), site.getLogo(), false);
    }
    
    public ProgrammaGemist(String naam, Site site, String url, String imgUrl, boolean losseAfleveringen) {
        super(naam, null);
        this.site = site;
        this.url = url;
        this.imgUrl = imgUrl;
        this.losseAfleveringen = losseAfleveringen;
    }

    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return downloadAndSend(imgUrl, true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }
    

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        String programmaPagina = HTTPWrapper.Request(url);

        //Filter op alleen de pagina's in het overzicht
        Matcher m1 = Pattern.compile("(?s)(Programma gemist overzicht|Bekijk alle afleveringen).*?class=\"bottom\"").matcher(programmaPagina);
        if (m1.find()) {
            programmaPagina = m1.group();
        }

        //Zoek shows / afleveringen
        Matcher m = Pattern.compile("(?s)class=\"thumb\".*?href=\"(/web/show.*?)\".*?src=\"(.*?)\".*?<span>(.*?)</span>").matcher(programmaPagina);

        //TODO: loop 'volgende' links af
        while (m.find()) {
            String programmaUrl = site.base + m.group(1);
            String img = m.group(2);
            if (!img.startsWith("http")) {
                img = site.base + img;
            }
            String programmaNaam = m.group(3);

            if (losseAfleveringen) {
                addChild(new ProgrammaStream(programmaNaam, programmaUrl, img));
            } else {
                addChild(new ProgrammaGemist(programmaNaam, site, programmaUrl, img, true));
            }

        }
    }

    //Stream die vlak voor afspelen de .URL goed zet
    public class ProgrammaStream extends WebStream {
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

                Matcher m = Pattern.compile("(?s)class=\"wmv-player-holder\" href=\"(.*?)\"").matcher(pagina);
                m.find();

                //WMV file is altijd maar een verwijzing naar mms:// url die er in zit
                this.URL = mms = new AsxFile(m.group(1)).getMediaStream();
                PMS.minimal("Mediastream: " + URL);
            }

            return super.getInputStream(low, high, timeseek, mediarenderer);
        }
    }

}
