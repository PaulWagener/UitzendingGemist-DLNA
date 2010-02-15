package net.pms.uitzendinggemist.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsxFile {

    private String XMLcontent;
    private String URL;

    public AsxFile(String URL) {
        this.URL = URL;
        this.XMLcontent = HTTPWrapper.Request(URL);
    }

    public String getMediaStream() {
        Matcher m = Pattern.compile("(?s)\"(mms://.*?)\"").matcher(XMLcontent);
        if (!m.find()) {
            return URL;
        }
        
        return m.group(1);
    }
}
