package net.pms.uitzendinggemist.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Job
 */
public class XmlFile {

    private String XMLcontent;

    public XmlFile(String XMLcontent) {
        this.XMLcontent = XMLcontent;
    }

    private String find(String regex) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(XMLcontent);
        if (!m.find()) {
            return null;
        }
        return m.group(1).trim();
    }

    private String find(String regex, int group) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(XMLcontent);
        if (!m.find()) {
            return null;
        }
        return m.group(group).trim();
    }

    public String getMediaStream() {
        String wmvUrl = find("file:'((.*?).wmv)\'");

        // Cut HTTP and replace it with MMS
        if (wmvUrl != null && wmvUrl.length() >= 4) {
            String buildMMS = "mms" + wmvUrl.substring(4);
            return buildMMS;
        }

        return null;
    }
}
