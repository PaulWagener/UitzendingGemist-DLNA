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
class TitelFolder extends VirtualFolder {

    private int titel;
    public TitelFolder(String string, int titel) {
        super(string, null);
        this.titel = titel;
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();


        List<Uitzending> uitzendingen = UitzendingGemistSite.getUitzendingen("http://www.uitzendinggemist.nl/index.php/selectie?searchitem=titel&titel=" + titel);
        for(Uitzending uitzending : uitzendingen)
            addChild(uitzending);
        // */

    }


}
