package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.uitzendinggemist.web.UitzendingGemistSite;

/**
 *
 * @author Paul Wagener
 */
public class UitzendingFolder extends VirtualFolder {

    String url;
    String post;
    String logo;

    public UitzendingFolder(String naam, String url) {
        this(naam, url, "", null);
    }

    public UitzendingFolder(String naam, String url, String post) {
        this(naam, url, post, null);
    }

    public UitzendingFolder(String naam, String url, String post, String logo) {
        super(naam, null);
        this.url = url;
        this.post = post;
        this.logo = logo;
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        List<Uitzending> uitzendingen = UitzendingGemistSite.getUitzendingen(url, post);

        for (Uitzending uitzending : uitzendingen) {
            addChild(uitzending);
        }
    }

    @Override
    public InputStream getThumbnailInputStream() {
       if (logo != null) {
            try {
                return downloadAndSend(logo, true);
            } catch (IOException ex) {
            }
        }
        return super.getThumbnailInputStream();

    }
}
