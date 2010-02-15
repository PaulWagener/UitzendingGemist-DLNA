/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.UitzendingGemistSite;

/**
 *
 * @author Paul Wagener
 */
public class Uitzending extends WebStream {


    public String afleveringID;

    public Uitzending(String name, String afleveringID, long time) {
        super(name, "mms://url.url/url", null, Format.VIDEO);
        this.afleveringID = afleveringID;
        this.lastmodified = time;
    }

    public String toString() {
        return getName() + " (" + afleveringID + ")";
    }

    String mms = null;
    @Override
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
        if(mms == null)
            this.URL = mms = UitzendingGemistSite.getStreamByAfleveringID(afleveringID);
        
        return super.getInputStream(low, high, timeseek, mediarenderer);
    }   
}
