package org.openstreetmap.josm.plugins.dxfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;

/**
 * 
 */
public class DxfImporter extends FileImporter {

    public DxfImporter() {
        super(new ExtensionFileFilter("dxf", "dxf",tr("DXF files [ImportDxf plugin] (*.dxf)")));
    }

    @Override
    public boolean isBatchImporter() {
        return false;
    }
    
    //    old importData method, i've limited the loading of files to one for now, maybe later i'll implement importing multiple files.
    // it can be done
    
//    @Override
//    public void importData(List<File> files, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
//        if (!Main.main.hasEditLayer()) {
//            JOptionPane.showMessageDialog(Main.parent, tr("Please open or create data layer before importing"));
//            return;
//        }
//        ImportDialog dlg = new ImportDialog();
//        if (dlg.getValue() != 1) return;
//        dlg.saveSettings();
//        Main.worker.submit(new SvgImportTask(files));
//    }
    
    @Override
    public void importData(final File file, ProgressMonitor progressMonitor) throws IOException, IllegalDataException {
        if (!Main.main.hasEditLayer()) {
            GuiHelper.runInEDT(new Runnable() {
                public void run() {
                    new Notification(tr("Please open or create data layer before importing")).show();
                }
            });
            return;
        }
        GuiHelper.runInEDTAndWait(new Runnable() {
            public void run() {
                ImportDialog dlg = new ImportDialog();
                if (dlg.getValue() != 1) return;
                dlg.saveSettings();
                Main.worker.submit(new DxfImportTask(file));
            }
        });
    }
}
