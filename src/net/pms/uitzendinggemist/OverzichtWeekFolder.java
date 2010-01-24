/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.pms.uitzendinggemist;

import net.pms.dlna.virtual.VirtualFolder;

/**
 *
 * @author Paul Wagener
 */
public class OverzichtWeekFolder extends VirtualFolder {

    public OverzichtWeekFolder()
    {
        super("Uitzending Gemist", null);
        addChild(new OverzichtDagFolder("Vandaag", "vandaag"));
        addChild(new OverzichtDagFolder("Gisteren", "gisteren"));
        addChild(new OverzichtDagFolder("Maandag", "1"));
        addChild(new OverzichtDagFolder("Dinsdag", "2"));
        addChild(new OverzichtDagFolder("Woensdag", "3"));
        addChild(new OverzichtDagFolder("Donderdag", "4"));
        addChild(new OverzichtDagFolder("Vrijdag", "5"));
        addChild(new OverzichtDagFolder("Zaterdag", "6"));
        addChild(new OverzichtDagFolder("Zondag", "0"));
    }


}
