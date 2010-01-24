/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.pms.uitzendinggemist;

import javax.swing.JComponent;
import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalResourceFolderListener;
import net.pms.external.ExternalListener;
import net.pms.external.StartStopListener;

/**
 *
 * @author Paul Wagener
 */
public class UitzendingGemist implements ExternalListener {

    public UitzendingGemist() {
        PMS.get().getRootFolder(null).addChild(new OverzichtWeekFolder());
    }

    public JComponent config() {
        return null; 
    }

    public String name() {
        return "Uitzending Gemist";
    }

    public void shutdown() {
        
    }
}
