package net.pms.uitzendinggemist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.dlna.virtual.VirtualFolder;
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

    public static void main(String args[]) {
        // Number of days back in time
        int daysBackInTime = 1;
        String streamsXml = HTTPWrapper.Request("http://np.rtl.nl/s4m/ajax/fun=vcr_content_in_time/backdays=" + daysBackInTime);

        Matcher m = Pattern.compile("(?s)<s4m:episode.*?<name>(.*?)</name>.*?<component_uri>(.*?)</component_uri>.*?</s4m:episode>").matcher(streamsXml);

        // Prefix for the urls is the domain of RTL
        String rtlUrl = "http://www.rtl.nl/";
        while (m.find()) {
            String name = m.group(1);
            String url = m.group(2);

            // Some shows have no url yet (they are not yet visable online), there is a <expect_datetime> maybe we can filter in this in the regex
            if (url != null && !url.equals("")) {
                String requestUrl = rtlUrl + url;
                String MMSUrl = new XmlFile(HTTPWrapper.Request(requestUrl)).getMediaStream();

                System.out.println(name + " \n" + MMSUrl + "\n");
            }
        }
    }
}

