package com.gregorbyte.xsp.library;

import com.ibm.xsp.library.AbstractXspLibrary;

public class BundleInspectorXspLibrary extends AbstractXspLibrary {

	public static final String PLUGIN_ID = "com.gregorbyte.xsp.bundleinspector";
	public static final String LIBRARY_ID = "com.gregorbyte.xsp.bundleinspector.library";

	public BundleInspectorXspLibrary() {

	}

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String[] getFacesConfigFiles() {

		return new String[] { "com/gregorbyte/xsp/config/gregorbyte-bundles-faces-config.xml" };

	}

	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	@Override
	public boolean isGlobalScope() {
		return true;
	}

}
