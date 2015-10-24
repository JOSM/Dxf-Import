package org.openstreetmap.josm.plugins.dxfimport;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DxfImportPlugin extends Plugin {

	public DxfImportPlugin(PluginInformation info) {
		super(info);
//		enableLogFile();
		ExtensionFileFilter.importers.add(new DxfImporter()); // adding the file importer to josm file filter
	}

/*	private void enableLogFile() {
		String logFile = "D:\\logFile.txt";
		try {
			if (Files.exists(Paths.get(logFile))) {
				Files.delete(Paths.get(logFile));
			}
			Files.createFile(Paths.get(logFile));
			PrintStream stream = new PrintStream(new File(logFile));
			System.setOut(stream);
			System.setErr(stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

}
