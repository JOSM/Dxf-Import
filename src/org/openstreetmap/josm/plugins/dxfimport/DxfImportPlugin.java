package org.openstreetmap.josm.plugins.dxfimport;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DxfImportPlugin extends Plugin {

	public DxfImportPlugin(PluginInformation info) {
		super(info);
		ExtensionFileFilter.addImporter(new DxfImporter()); // adding the file importer to josm file filter
	}
}
