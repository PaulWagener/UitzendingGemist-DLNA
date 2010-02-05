/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pms.uitzendinggemist.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pms.PMS;
import net.pms.uitzendinggemist.Uitzending;

/**
 *
 * @author Paul Wagener
 */
abstract public class UitzendingGemistSite {

    public static List<Uitzending> getUitzendingen(String URL) {
        return getUitzendingen(URL, "");
    }

    public static List<Uitzending> getUitzendingen(String url, String post) {
        //Get cookie!
        HTTPWrapper.Request("http://www.uitzendinggemist.nl/");

        List<Uitzending> uitzendingen = new ArrayList<Uitzending>();
        addUitzendingen(url, post, uitzendingen, 1);

        return uitzendingen;
    }

    private static void addUitzendingen(String url, String post, List<Uitzending> uitzendingen, int pgNum) {
        String lijstPagina = HTTPWrapper.Request(url + "&pgNum=" + pgNum, post);
        Matcher m = Pattern.compile("<td[^<]*?><a[^<]*?md5=.*?>(.*?)</a>.*?<td.*?>(.*?)</td>.*?<a href=\"http://player.omroep.nl/\\?aflID=(.*?)\"", Pattern.DOTALL).matcher(lijstPagina);

        while (m.find()) {
            long time = 0;
            try {
                time = new SimpleDateFormat("dd-MM-yyyy").parse(m.group(2)).getTime();
            } catch (ParseException e) {
            }

            uitzendingen.add(new Uitzending(m.group(1), m.group(3), time));
        }

        //Is er een volgende pagina?
        if (lijstPagina.indexOf(">volgende<") != -1) {
            addUitzendingen(url, post, uitzendingen, pgNum + 1);
        }
    }

    public static String getStreamByAfleveringID(String afleveringID) {
        PMS.minimal("Aflevering " + afleveringID);
        /**
         * === Ophalen Sessiecookie ===
         */
        //Bestanden op uitzendinggemist geven een foutmelding als je ze geen cookie geeft
        //Deze cookie is op te halen op een willekeurige pagina op player.omroep.nl
        HTTPWrapper.strCookies = "";
        HTTPWrapper.Request("http://player.omroep.nl/?aflID=" + afleveringID);


        /**
         *  === Ophalen Security Code ===
         */
        // UitzendingGemist handhaaft een securitycode die in een javascript bestand
        // Dit is een md5 code die verplicht is mee te geven aan de metaplayer.xml.php pagina
        //
        String jsbestandinhoud = HTTPWrapper.Request("http://player.omroep.nl/js/initialization.js.php?aflID=" + afleveringID);

        String securityCode = find("var securityCode = '(.*?)';", jsbestandinhoud);

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
        MetaplayerInfo metainfo = new MetaplayerInfo(metaXML);
        PMS.minimal("Gegevens aflevering:\n" + "  Titel: " + metainfo.getTitel() + "\n" + "  Duratie: " + metainfo.getDuratie() + "\n" + "  Stream: " + metainfo.getBBStream() + "\n");

        String streamURL = metainfo.getBBStream();
        if (streamURL == null) {
            PMS.minimal("Deze aflevering is niet bekend bij uitzendinggemist.nl");
        }

        /**
         * === Downloaden Stream Informatie ===
         */
        // -- Stream bestand --
        // De URL die door Windows Media Player wordt gebruikt om streams af te
        // spelen kan niet door mplayer worden afgespeeld. Dit komt omdat het
        // eigenlijk een klein bestand is met een verwijzing naar de
        // streamserver. De stream zelf heeft een URL van het formaat
        // mms://server/etc.asf
        String streamXML = HTTPWrapper.Request(streamURL);
        String mediastream = new StreamInfo(streamXML).getMediaStream();

        if (mediastream == null) {
            mediastream = streamXML;
        }

        PMS.minimal("mediastream: " + mediastream);

        return mediastream;
    }

    private static String find(String regex, String XMLcontent) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(XMLcontent);
        if (!m.find()) {
            return null;
        }
        return m.group(1).trim();
    }
}
