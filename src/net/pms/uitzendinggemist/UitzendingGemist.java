/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.pms.uitzendinggemist;

import javax.swing.JComponent;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.external.AdditionalFolderAtRoot;

/**
 *
 * @author paulwagener
 */
public class UitzendingGemist implements AdditionalFolderAtRoot {

    public DLNAResource getChild() {
        VirtualFolder mainFolder = new VirtualFolder("Uitzending Gemist", null);
        mainFolder.addChild(new Omroep());
        mainFolder.addChild(new RTLGemist()); // Includes all RTL channels
        mainFolder.addChild(new ProgrammaGemist("Net 5", ProgrammaGemist.NET5_URL));
        mainFolder.addChild(new ProgrammaGemist("SBS 6", ProgrammaGemist.SBS6_URL));        
        mainFolder.addChild(new ProgrammaGemist("Veronica", ProgrammaGemist.VERONICA_URL));
        return mainFolder;
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
