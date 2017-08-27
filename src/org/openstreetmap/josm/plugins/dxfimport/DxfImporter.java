// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.dxfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.IllegalDataException;

/**
 * DXF file importer 
 */
public class DxfImporter extends FileImporter {

    public DxfImporter() {
        super(new ExtensionFileFilter("dxf", "dxf", tr("DXF files [ImportDxf plugin] (*.dxf)")));
    }

    @Override
    public boolean isBatchImporter() {
        return false;
    }
    
    @Override
    public void importData(final File file, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
        if (MainApplication.getLayerManager().getEditLayer() == null) {
            GuiHelper.runInEDT(() -> new Notification(tr("Please open or create data layer before importing")).show());
            return;
        }
        GuiHelper.runInEDTAndWait(() -> {
            ImportDialog dlg = new ImportDialog();
            if (dlg.getValue() != 1) return;
            dlg.saveSettings();
            MainApplication.worker.submit(new DxfImportTask(file));
        });
    }
}
