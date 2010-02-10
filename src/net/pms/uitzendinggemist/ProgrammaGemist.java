/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 *
 * @author paulwagener
 */
public class ProgrammaGemist extends VirtualFolder {

    public final static String SBS6_URL = "http://www.sbs6.nl/web/show/id=73863/langid=43";
    public final static String NET5_URL = "http://www.net5.nl/web/show/id=95681/langid=43";
    public final static String VERONICA_URL = "http://www.veronicatv.nl/web/show/id=96520/langid=43";

    public ProgrammaGemist(String naam, String URL) {
        this(naam, URL, false);
    }
    String naam;
    String url;
    boolean losseAfleveringen;

    public ProgrammaGemist(String naam, String URL, boolean losseAfleveringen) {
        super(naam, null);
        this.url = URL;
        this.losseAfleveringen = losseAfleveringen;
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        String programmaPagina = HTTPWrapper.Request(url);

        //Filter op overzicht
        Matcher m1 = Pattern.compile("(?s)(Programma gemist overzicht|Bekijk alle afleveringen).*?class=\"bottom\"").matcher(programmaPagina);
        if (m1.find()) {
            programmaPagina = m1.group();
        }

        //Zoek shows
        Matcher m = Pattern.compile("(?s)class=\"thumb\".*?href=\"(/web/show.*?)\".*?src=\"(.*?)\".*?<span>(.*?)</span>").matcher(programmaPagina);

        while (m.find()) {
            if (losseAfleveringen) {
                addChild(new ProgrammaStream(m.group(3), m.group(1), m.group(2)));
            } else {
                addChild(new ProgrammaGemist(m.group(3), m.group(1), true));
            }

        }
    }

    static class ProgrammaStream extends WebStream {
        String locatieUrl; //Webpagina waar filmpje te bekijken is
        String echteUrl = null; //URL van wmv file

        public ProgrammaStream(String naam, String url, String img) {
            super(naam, "mms://url.invalid/url", img, Format.VIDEO);
            this.locatieUrl = url;
        }        

        @Override
        public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
            if (echteUrl == null) {
                String pagina = HTTPWrapper.Request("http://www.sbs6.nl" + locatieUrl);

                Matcher m = Pattern.compile("(?s)class=\"wmv-player-holder\" href=\"(.*?))\"").matcher(pagina);
                m.find();

                this.URL = echteUrl = m.group();
            }

            return super.getInputStream(low, high, timeseek, mediarenderer);
        }
    }
}
