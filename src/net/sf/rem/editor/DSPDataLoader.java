/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.rem.editor;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class DSPDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-dsp+xml";
    private static final long serialVersionUID = 1L;

    public DSPDataLoader() {
        super("net.sf.rem.editor.DSPDataObject");
    }

    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(DSPDataLoader.class, "LBL_DSP_loader_name");
    }

    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new DSPDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
}
