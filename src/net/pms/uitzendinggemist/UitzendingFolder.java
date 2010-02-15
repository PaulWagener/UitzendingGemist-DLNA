/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.pms.uitzendinggemist;

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

    public UitzendingFolder(String naam, String url)
    {
        this(naam, url, "");
    }

    public UitzendingFolder(String naam, String url, String post) {
        super(naam, null);
        this.url = url;
        this.post = post;
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        List<Uitzending> uitzendingen = UitzendingGemistSite.getUitzendingen(url, post);
        
        for(Uitzending uitzending : uitzendingen) {
            //System.out.println(uitzending);
            addChild(uitzending);
        }
    }
}
