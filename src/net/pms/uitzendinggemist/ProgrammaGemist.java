package net.pms.uitzendinggemist;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.MatchResult;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.Range;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.uitzendinggemist.web.HTTPWrapper;

/**
 *
 * @author paulwagener
 */
public class ProgrammaGemist extends VirtualFolder {

    static int[] bytes1 = {0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00,
        0x46, 0x63, 0x6F, 0x6D, 0x2E, 0x62, 0x72, 0x69, 0x67, 0x68, 0x74, 0x63,
        0x6F, 0x76, 0x65, 0x2E, 0x65, 0x78, 0x70, 0x65, 0x72, 0x69, 0x65, 0x6E,
        0x63, 0x65, 0x2E, 0x45, 0x78, 0x70, 0x65, 0x72, 0x69, 0x65, 0x6E, 0x63,
        0x65, 0x52, 0x75, 0x6E, 0x74, 0x69, 0x6D, 0x65, 0x46, 0x61, 0x63, 0x61,
        0x64, 0x65, 0x2E, 0x67, 0x65, 0x74, 0x44, 0x61, 0x74, 0x61, 0x46, 0x6F,
        0x72, 0x45, 0x78, 0x70, 0x65, 0x72, 0x69, 0x65, 0x6E, 0x63, 0x65, 0x00,
        0x02, 0x2F, 0x31, 0x00, 0x00, 0x01, 0xBB, 0x0A, 0x00, 0x00, 0x00, 0x02,
        0x02, 0x00, 0x28, 0x30, 0x35, 0x35, 0x39, 0x32, 0x32, 0x39, 0x35, 0x36,
        0x34, 0x66, 0x61, 0x35, 0x35, 0x61, 0x32, 0x36, 0x36, 0x65, 0x65, 0x65,
        0x61, 0x63, 0x34, 0x62, 0x38, 0x39, 0x61, 0x35, 0x62, 0x39, 0x66, 0x37,
        0x35, 0x35, 0x36, 0x38, 0x33, 0x38, 0x32, 0x11, 0x0A, 0x63, 0x63, 0x63,
        0x6F, 0x6D, 0x2E, 0x62, 0x72, 0x69, 0x67, 0x68, 0x74, 0x63, 0x6F, 0x76,
        0x65, 0x2E, 0x65, 0x78, 0x70, 0x65, 0x72, 0x69, 0x65, 0x6E, 0x63, 0x65,
        0x2E, 0x56, 0x69, 0x65, 0x77, 0x65, 0x72, 0x45, 0x78, 0x70, 0x65, 0x72,
        0x69, 0x65, 0x6E, 0x63, 0x65, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74,
        0x19, 0x65, 0x78, 0x70, 0x65, 0x72, 0x69, 0x65, 0x6E, 0x63, 0x65, 0x49,
        0x64, 0x11, 0x54, 0x54, 0x4C, 0x54, 0x6F, 0x6B, 0x65, 0x6E, 0x19, 0x64,
        0x65, 0x6C, 0x69, 0x76, 0x65, 0x72, 0x79, 0x54, 0x79, 0x70, 0x65, 0x21,
        0x63, 0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x4F, 0x76, 0x65, 0x72, 0x72,
        0x69, 0x64, 0x65, 0x73, 0x13, 0x70, 0x6C, 0x61, 0x79, 0x65, 0x72, 0x4B,
        0x65, 0x79, 0x07, 0x55, 0x52, 0x4C, 0x05, 0x42, 0x70, 0xBD, 0xB4, 0x34,
        0x94, 0x10, 0x00, 0x06, 0x01, 0x05, 0x7F, 0xFF, 0xFF, 0xFF, 0xE0, 0x00,
        0x00, 0x00, 0x09, 0x03, 0x01, 0x0A, 0x81, 0x03, 0x53, 0x63, 0x6F, 0x6D,
        0x2E, 0x62, 0x72, 0x69, 0x67, 0x68, 0x74, 0x63, 0x6F, 0x76, 0x65, 0x2E,
        0x65, 0x78, 0x70, 0x65, 0x72, 0x69, 0x65, 0x6E, 0x63, 0x65, 0x2E, 0x43,
        0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x4F, 0x76, 0x65, 0x72, 0x72, 0x69,
        0x64, 0x65, 0x15, 0x63, 0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x49, 0x64,
        0x73, 0x13, 0x63, 0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x49, 0x64, 0x0D,
        0x74, 0x61, 0x72, 0x67, 0x65, 0x74, 0x17, 0x63, 0x6F, 0x6E, 0x74, 0x65,
        0x6E, 0x74, 0x54, 0x79, 0x70, 0x65, 0x19, 0x63, 0x6F, 0x6E, 0x74, 0x65,
        0x6E, 0x74, 0x52, 0x65, 0x66, 0x49, 0x64, 0x1B, 0x66, 0x65, 0x61, 0x74,
        0x75, 0x72, 0x65, 0x64, 0x52, 0x65, 0x66, 0x49, 0x64, 0x1B, 0x63, 0x6F,
        0x6E, 0x74, 0x65, 0x6E, 0x74, 0x52, 0x65, 0x66, 0x49, 0x64, 0x73, 0x15,
        0x66, 0x65, 0x61, 0x74, 0x75, 0x72, 0x65, 0x64, 0x49, 0x64, 0x01, 0x05};
    //0x42, 0x71, 0xFD, 0xC6, 0x59, 0x9E, 0x10, 0x00,
    static int[] bytes2 = {0x06, 0x17, 0x76, 0x69,
        0x64, 0x65, 0x6F, 0x50, 0x6C, 0x61, 0x79, 0x65, 0x72, 0x04, 0x00, 0x01,
        0x01, 0x01, 0x05, 0x7F, 0xFF, 0xFF, 0xFF, 0xE0, 0x00, 0x00, 0x00, 0x06,
        0x65, 0x41, 0x51, 0x7E, 0x7E, 0x2C, 0x41, 0x41, 0x41, 0x41, 0x69, 0x44,
        0x65, 0x6E, 0x42, 0x55, 0x6B, 0x7E, 0x2C, 0x59, 0x74, 0x6E, 0x78, 0x76,
        0x42, 0x78, 0x47, 0x4F, 0x30, 0x33, 0x41, 0x76, 0x39, 0x72, 0x72, 0x43,
        0x33, 0x56, 0x57, 0x48, 0x36, 0x48, 0x59, 0x42, 0x67, 0x38, 0x37, 0x55,
        0x45, 0x30, 0x49, 0x06, 0x33, 0x68, 0x74, 0x74, 0x70, 0x3A, 0x2F, 0x2F,
        0x77, 0x77, 0x77, 0x2E, 0x76, 0x65, 0x72, 0x6F, 0x6E, 0x69, 0x63, 0x61,
        0x74, 0x76, 0x2E, 0x6E, 0x6C, 0x2F};

    public static void main(String args[]) {
        //Zoek videoPlayer variabele in source
        String masterchef = HTTPWrapper.Request("http://www.sbs6.nl/programmas/trauma-centrum/videos/seizoen-0/aflevering-345/trauma-centrum-345");
        String videoPlayerIdString = Regex.get("videoPlayer\\\\\" value=\\\\\"([0-9]+)\\\\\"", masterchef);
        double videoPlayerId = Double.parseDouble(videoPlayerIdString);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


        try {
            DataOutputStream k = new DataOutputStream(byteArrayOutputStream);

            for (int i = 0; i < bytes1.length; i++) {
                k.write(bytes1[i]);
            }

            k.writeDouble(videoPlayerId);

            for (int i = 0; i < bytes2.length; i++) {
                k.write(bytes2[i]);
            }

            k.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        byte bbb[] = byteArrayOutputStream.toByteArray();

        String asmSource = HTTPWrapper.Request("http://c.brightcove.com/services/messagebroker/amf?playerKey=AQ~~,AAAAiDenBUk~,YtnxvBxGO03Av9rrC3VWH6HYBg87UE0I", bbb);
        String rtmpStream = Regex.get("(rtmp://.*?\\.mp4)", asmSource);
        rtmpStream = rtmpStream.replace("&", "");
        System.out.println(rtmpStream);
    }

    /**
     * Een site is een kanaal zoals Veronica, SBS 6 of Net 5
     */
    public enum Site {

        SBS6("http://www.sbs6.nl", "/static/gfx/logoSbs6.png"),
        VERONICA("http://www.veronicatv.nl", "/static/gfx/logoVeronica.png"),
        NET5("http://www.net5.nl", "/static/gfx/logoNet5.png");
        public String base;
        public String logo;

        public String getLogo() {
            return base + logo;
        }

        Site(String base, String logo) {
            this.base = base;
            this.logo = logo;
        }
    }
    Site site;

    public ProgrammaGemist(String naam, Site site) {
        super(naam, null);
        this.site = site;
    }

    @Override
    public InputStream getThumbnailInputStream() {
        try {
            return downloadAndSend(site.getLogo(), true);
        } catch (IOException ex) {
            return super.getThumbnailInputStream();
        }
    }

    /**
     * Zoek de seriess op dit kanaal
     */
    @Override
    public void discoverChildren() {
        super.discoverChildren();

        String pageLink = "/ajax/programFilter/range/abcdefghijklmonpqrstuvwxyz/day/0/genre/all/block/gemist";
        String pageSource = null;
        do {
            pageSource = HTTPWrapper.Request(site.base + pageLink);

            //Voeg alle series op de page toe


            for (MatchResult serie : Regex.all("<img src=\"(.*?)\".*?<h2><a href=\"(.*?)\">(.*?)</a>", pageSource)) {
                addChild(new Serie(serie.group(3), serie.group(2), serie.group(1), true, site));
            }

            //Zolang er een 'nextPage' is
        } while ((pageLink = Regex.get("<a href=\"([a-zA-Z0-9-_/]+)\" class=\"nextPage", pageSource)) != null);
    }

    /**
     *
     */
    static class Serie extends VirtualFolder {

        String url;
        Site site;
        String image;
        boolean findSeasons;

        public Serie(String naam, String url, String image, boolean findSeasons, Site site) {
            super(naam, null);
            this.url = url;
            this.image = image;
            this.site = site;
            this.findSeasons = findSeasons;
        }

        @Override
        public void discoverChildren() {
            super.discoverChildren();


            String serieSource = HTTPWrapper.Request(site.base + url);
            System.out.println(site.base + url);
            if (findSeasons) {
                String submenuHtml = Regex.get("class=\"subMenu\"(.*?)class=\"backgroundWrapper\"", serieSource);


                //Een serie kan seizoenen hebben, in dat geval losse subseries maken
                if (submenuHtml != null) {
                    for (MatchResult m : Regex.all("href=\"(/.*?)\">(.*?)<", submenuHtml)) {
                        addChild(new Serie(m.group(2), m.group(1), null, false, site));
                    }

                    return;
                }
            }

            //voeg afleveringen uit de pagina toe
            String afleveringenHtml = Regex.get("<h1>Afleveringen</h1>(.*?)<header", serieSource);

            for (MatchResult m : Regex.all("<a href=\"(/.*?)\".*?src=\"(.*?)\".*?<h2>(.*?)</h2>", afleveringenHtml)) {
                addChild(new ProgrammaStream(m.group(3), site.base + m.group(1), m.group(2)));
            }

        }

        @Override
        public InputStream getThumbnailInputStream() {
            try {
                return downloadAndSend(site.base + image, true);
            } catch (IOException ex) {
                return super.getThumbnailInputStream();
            }
        }
    }

    //Stream die vlak voor afspelen de .URL goed zet
    public static class ProgrammaStream extends WebStream {

        String locatieUrl; //Webpagina waar filmpje te bekijken is
        String mms = null; //mms stream

        public ProgrammaStream(String naam, String url, String img) {
            super(naam, "mms://url.url/url", img, Format.VIDEO);
            this.locatieUrl = url;
        }

        @Override
        public InputStream getInputStream(Range range, RendererConfiguration mediarenderer) throws IOException {
            //Zoek videoPlayer variabele in source
            String masterchef = HTTPWrapper.Request(locatieUrl);
            String videoPlayerIdString = Regex.get("videoPlayer\\\\\" value=\\\\\"([0-9]+)\\\\\"", masterchef);
            double videoPlayerId = Double.parseDouble(videoPlayerIdString);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


            try {
                DataOutputStream k = new DataOutputStream(byteArrayOutputStream);

                for (int i = 0; i < bytes1.length; i++) {
                    k.write(bytes1[i]);
                }

                k.writeDouble(videoPlayerId);

                for (int i = 0; i < bytes2.length; i++) {
                    k.write(bytes2[i]);
                }

                k.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            byte bbb[] = byteArrayOutputStream.toByteArray();

            String asmSource = HTTPWrapper.Request("http://c.brightcove.com/services/messagebroker/amf?playerKey=AQ~~,AAAAiDenBUk~,YtnxvBxGO03Av9rrC3VWH6HYBg87UE0I", bbb);
            String rtmpStream = Regex.get("(rtmp://.*?\\.mp4)", asmSource);
            rtmpStream = rtmpStream.replace("&", "");
            
            this.url = rtmpStream;

            return super.getInputStream(range, mediarenderer);
        }
    }
}
