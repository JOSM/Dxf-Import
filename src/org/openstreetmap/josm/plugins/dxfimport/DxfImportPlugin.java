package org.openstreetmap.josm.plugins.dxfimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DxfImportPlugin extends Plugin {

	public DxfImportPlugin(PluginInformation info) {
		super(info);
//		enableLogFile();
		ExtensionFileFilter.importers.add(new DxfImporter());
	}

	private void enableLogFile() {
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
	}

}
