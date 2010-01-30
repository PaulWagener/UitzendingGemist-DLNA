/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.pms.uitzendinggemist;

import java.util.List;
import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.uitzendinggemist.Uitzending;
import net.pms.uitzendinggemist.web.UitzendingGemistSite;

/**
 *
 * @author Paul Wagener
 */
public class OverzichtDagFolder extends VirtualFolder {

    private String dagWaarde;
    public OverzichtDagFolder(String dagNaam, String dagWaarde)
    {
        super(dagNaam, null);
        this.dagWaarde = dagWaarde;
    }

    @Override
    public void discoverChildren() {
        super.discoverChildren();

        List<Uitzending> uitzendingen = UitzendingGemistSite.getUitzendingen("http://www.uitzendinggemist.nl/index.php/selectie?searchitem=dag&dag=" + dagWaarde);
        for(Uitzending uitzending : uitzendingen) {
            addChild(uitzending);
        }
    }




}
