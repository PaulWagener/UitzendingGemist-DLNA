package net.pms.uitzendinggemist.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deze klasse neemt de berg aan XML die afkomstig is van een
 * http://player.omroep.nl/xml/metaplayer.xml.php URL en kan er de
 * relevante informatie uithalen
 *
 * @author Paul Wagener
 */
public class MetaplayerInfo {

    private String XMLcontent;

    public MetaplayerInfo(String XMLcontent) {
        this.XMLcontent = XMLcontent;
    }

    private String find(String regex) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(XMLcontent);
        if (!m.find()) {
            return null;
        }
        return m.group(1).trim();
    }

    public String getStream() {
        //Probeer eerst hogekwaliteits stream te zoeken
        String stream = find("<stream compressie_kwaliteit='std' compressie_formaat='wvc1'>(.*?)</stream>");

        if (stream == null) {
            stream = find("<stream .*?bb.*?wmv.*?>(.*?)</stream>");
        }
        return stream;
    }

    public String getTitel() {
        return find("<tite>(.*?)</tite>");
    }

    public String getIcon() {
        return find("<icon>(.*?)</icon>");
    }

    public String getDuratie() {
        return find("<duration>(.*?)</duration>");
    }
}
