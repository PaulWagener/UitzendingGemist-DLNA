package net.pms.uitzendinggemist;

import net.pms.uitzendinggemist.web.Regex;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 * Browse door alle programma's die op rtl.nl staan
 *
 * @author Paul Wagener
 */
public class RTLGemist extends VirtualFolder {

    public static void main(String args[]) {
        new RTLGemist().discoverChildren();
    }

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
        //super.discoverChildren();

        Map<Date, VirtualFolder> datums = new HashMap<Date, VirtualFolder>();
        Map<String, VirtualFolder> series = new HashMap<String, VirtualFolder>();

        String xml = HTTPWrapper.Request("http://iptv.rtl.nl/xl/VideoItem/IpadXML");
        int i = 0;
        for (MatchResult m : Regex.all("<item>.*?<title>(.*?)</title>.*?<broadcastdatetime>(.*?)</broadcastdatetime>"
                + ".*?<thumbnail>(.*?)</thumbnail>.*?<movie>(.*?)</movie>.*?<serienaam>(.*?)</serienaam>.*?<classname>(.*?)</classname>", xml)) {
            String titel = m.group(1);

            String uitzendtijd = m.group(2).substring(0, 10).trim();
            String thumb = m.group(3).trim();
            String movie = m.group(4).trim();
            String serienaam = m.group(5).trim();
            String classname = m.group(6).trim();
            if (classname.equals("eps_fragment")) {
                continue;
            }

            
            //Voeg toe aan datum
            Date d = null;
            try {
                d = new SimpleDateFormat("yyyy-MM-dd").parse(uitzendtijd);
            } catch (ParseException ex) {
                Logger.getLogger(RTLGemist.class.getName()).log(Level.SEVERE, null, ex);
            }
            String uitzendtijdNL = new SimpleDateFormat("EEEE d MMMM").format(d);

            if (!datums.containsKey(d)) {
                datums.put(d, new VirtualFolder(uitzendtijdNL, null));
            }
            datums.get(d).addChild(new WebStream(titel, movie, thumb, Format.VIDEO));

            //Voeg toe aan serie
            if(!series.containsKey(serienaam)) {
                series.put(serienaam, new VirtualFolder(serienaam, null));
            }
            series.get(serienaam).addChild(new WebStream(titel, movie, thumb, Format.VIDEO));
        }

        VirtualFolder datumfolder = new VirtualFolder("Op datum", null);
        VirtualFolder seriefolder = new VirtualFolder("Op serie", null);


        ArrayList<Date> datumnamen = new ArrayList<Date>(datums.keySet());
        Collections.sort(datumnamen);
        Collections.reverse(datumnamen);
        
        for(Date datum : datumnamen) {
            datumfolder.addChild(datums.get(datum));
        }

        ArrayList<String> serienamen = new ArrayList<String>(series.keySet());
        Collections.sort(serienamen);

        for(String serienaam : serienamen) {
            seriefolder.addChild(series.get(serienaam));
        }

        this.addChild(datumfolder);
        this.addChild(seriefolder);
    }
}
