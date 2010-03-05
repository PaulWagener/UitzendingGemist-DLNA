/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.WebStream;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.AsxFile;
import net.pms.uitzendinggemist.web.HTTPWrapper;
import net.pms.uitzendinggemist.web.MetaplayerInfo;

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

    String mms = null;

    @Override
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
        if (mms == null) {
            MetaplayerInfo info = getMetaInfo();
            PMS.minimal("Gegevens aflevering:\n" + "  Titel: " + info.getTitel() + "\n" + "  Duratie: " + info.getDuratie() + "\n" + "  Stream: " + info.getBBStream() + "\n");
            
            String stream = new AsxFile(info.getBBStream()).getMediaStream();
            PMS.minimal("Stream: " + stream);
            this.URL = mms = stream;
        }
        return super.getInputStream(low, high, timeseek, mediarenderer);
    }

    @Override
    public InputStream getThumbnailInputStream() throws IOException {
        String icon = getMetaInfo().getIcon();
        if(icon != null) {
            return downloadAndSend(info.getIcon(), true);
        } else {
            return super.getThumbnailInputStream();
        }
    }    

    MetaplayerInfo info;

    static String cookie = null;

    public synchronized MetaplayerInfo getMetaInfo() {
        if (info != null) {
            return info;
        }

        if(cookie == null) {
            HTTPWrapper.strCookies = "";
            HTTPWrapper.Request("http://player.omroep.nl/?aflID=" + afleveringID);
            cookie = HTTPWrapper.strCookies;
        } else {
            HTTPWrapper.strCookies = cookie;
        }
        /**
         *  === Ophalen Security Code ===
         */
        // UitzendingGemist handhaaft een securitycode die in een javascript bestand
        // Dit is een md5 code die verplicht is mee te geven aan de metaplayer.xml.php pagina
        //
        String jsbestandinhoud = HTTPWrapper.Request("http://player.omroep.nl/js/initialization.js.php?aflID=" + afleveringID);

        String securityCode = Regex.get("var securityCode = '(.*?)';", jsbestandinhoud);

        if (securityCode == null) {
            PMS.minimal("SecurityCode niet kunnen vinden in javascript");
        }
        /**
         * === Ophalen metaplayer.xml.php ===
         */
        // -- metaplayer.xml.php --
        // Genereer de URL waar afleveringgegevens zijn te downloaden
        // Deze URL wordt door player.omroep.nl gebruikt om XMLHttpRequest op
        // uit te voeren.
        // De XML bevat metagegevens zoals titel, datum van uitzending en
        // duratie.
        // Wij grabben het echter om er de URL van het Windows Media bestand
        // uit te halen.
        String metaplayerURL = "http://player.omroep.nl/xml/metaplayer.xml.php?aflID=" + afleveringID;

        // -- Security Code --
        // In Javascript wordt op player.omroep.nl een md5 code gegenereerd, deze wordt
        // via XmlHTTPRequest meegegeven aan metaplayer.xml.php. Als deze code verkeerd is
        // wordt er een foutmelding teruggegeven.
        metaplayerURL += "&md5=" + securityCode;

        // -- Cookies --
        // Mochten we grabs moeten uitvoeren op uitzendinggemist.nl om om deze
        // beveiligingsmaatregelen heen te komen dan is het belangrijk om
        // WebRequest uit te breiden met Cookie mogelijkheden, als cookies
        // uitstaan weigert uitzendinggemist.nl de juiste pagina terug te geven

        // Haal de gegevens van metaplayerXML op
        String metaXML = HTTPWrapper.Request(metaplayerURL);
        
        //Print info over aflevering
        this.info = new MetaplayerInfo(metaXML);
        return info;

    }
}
