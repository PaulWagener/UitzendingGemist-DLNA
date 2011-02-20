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
        addChild(new RtlDagFolder("Zaterdag"));
        addChild(new RtlDagFolder("Zondag"));
        addChild(new RtlDagFolder("Maandag"));
        addChild(new RtlDagFolder("Dinsdag"));
        addChild(new RtlDagFolder("Woensdag"));
        addChild(new RtlDagFolder("Donderdag"));
        addChild(new RtlDagFolder("Vrijdag"));

    }
    static class RtlDagFolder extends VirtualFolder {

        public RtlDagFolder(String naam) {
            super(naam, null);
            
        }
        
        @Override
        public void discoverChildren() {
            super.discoverChildren();
            String videomenu = HTTPWrapper.Request("http://antonboonstra.nl/rtlgemist/?" + this.getName().toLowerCase());

            for (MatchResult m : Regex.all("<li onclick=\"javascript:location.href='(.*?)';\">(<img.*?br>)?([^>]*?)<br>", videomenu)) {
                String url = m.group(1);
                String naam = m.group(3).replace("&amp;", "&");

                addChild(new WebStream(naam, url, null, Format.VIDEO));
            }
        }
    }
}
