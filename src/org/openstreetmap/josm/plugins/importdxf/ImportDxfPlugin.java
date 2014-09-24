package org.openstreetmap.josm.plugins.importdxf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class ImportDxfPlugin extends Plugin {

    public ImportDxfPlugin(PluginInformation info) {
        super(info);
        try {
        	if(Files.exists(Paths.get("D:\\tempFile.txt"))){
        		Files.delete(Paths.get("D:\\tempFile.txt"));
        	}
        	Files.createFile(Paths.get("D:\\tempFile.txt"));
        	PrintStream stream = new PrintStream(new File("D:\\tempFile.txt"));
			System.setOut(stream);
			System.setErr(stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	ExtensionFileFilter.importers.add(new DxfImporter());
    }

}
