package com.gregorbyte.xsp.config;

import com.ibm.xsp.library.AbstractXspLibrary;

public class GregorbyteXspLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = "com.gregorbyte.xsp.library";

	public static final String PLUGIN_ID = "com.gregorbyte.xsp.extlib";
	
	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public String[] getFacesConfigFiles() {
		return super.getFacesConfigFiles();
	}

	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	@Override
	public String[] getXspConfigFiles() {
		// TODO Auto-generated method stub
		return super.getXspConfigFiles();
	}

	@Override
	public boolean isGlobalScope() {
		return true;
	}

	
	
}
