package net.pms.uitzendinggemist;

import java.io.IOException;
import java.io.InputStream;
import javax.swing.JComponent;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.external.AdditionalFolderAtRoot;

/**
 * This class is the entry point for the UitzendingGemist plugin
 * It sets up the virtual folder containing all channels & episodes
 *
 * @author paulwagener
 */
public class UitzendingGemist implements AdditionalFolderAtRoot {

    public DLNAResource getChild() {
        VirtualFolder mainFolder = new VirtualFolder("Uitzending Gemist", null) {
            @Override
            public InputStream getThumbnailInputStream() {
                try {
                    return downloadAndSend("http://code.google.com/p/uitzendinggemist-dlna/logo?logo_id=1265826920", true);
                } catch (IOException ex) {
                    return super.getThumbnailInputStream();
                }
            }
        };
        mainFolder.addChild(new PubliekeOmroep());
        mainFolder.addChild(new RTLGemist()); // Includes all RTL channels

        mainFolder.addChild(new ProgrammaGemist("Net 5", ProgrammaGemist.Site.NET5));
        mainFolder.addChild(new ProgrammaGemist("SBS 6", ProgrammaGemist.Site.SBS6));
        mainFolder.addChild(new ProgrammaGemist("Veronica", ProgrammaGemist.Site.VERONICA));
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
