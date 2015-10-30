package com.gregorbyte.xsp.library;

import java.util.ArrayList;
import java.util.List;

import com.gregorbyte.xsp.config.ControlsConfig;
import com.gregorbyte.xsp.config.GregorbytePluginConfig;
import com.ibm.xsp.library.AbstractXspLibrary;

public class GregorbyteXspLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = "com.gregorbyte.xsp.library";

	public static final String PLUGIN_ID = "com.gregorbyte.xsp";

	private List<GregorbytePluginConfig> configs;

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String[] getFacesConfigFiles() {
		String[] files = new String[] {};
		List<GregorbytePluginConfig> plugins = getGregorbytePluginConfigs();
		for (GregorbytePluginConfig plugin : plugins) {
			files = plugin.getFacesConfigFiles(files);
		}
		return files;
	}

	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	@Override
	public String[] getDependencies() {
		return new String[] { "com.ibm.xsp.core.library", // $NON-NLS-1$
				"com.ibm.xsp.extsn.library", // $NON-NLS-1$
				"com.ibm.xsp.domino.library", // $NON-NLS-1$
				"com.ibm.xsp.designer.library", // $NON-NLS-1$
		};
	}

	@Override
	public String[] getXspConfigFiles() {

		String[] files = new String[] {};
		List<GregorbytePluginConfig> plugins = getGregorbytePluginConfigs();
		for (GregorbytePluginConfig plugin : plugins) {
			files = plugin.getXspConfigFiles(files);
		}
		return files;
	}

	@Override
	public boolean isGlobalScope() {
		return true;
	}

	public List<GregorbytePluginConfig> getGregorbytePluginConfigs() {

		if (configs == null) {

			configs = new ArrayList<GregorbytePluginConfig>();
			configs.add(new ControlsConfig());

		}

		return configs;

	}

}
