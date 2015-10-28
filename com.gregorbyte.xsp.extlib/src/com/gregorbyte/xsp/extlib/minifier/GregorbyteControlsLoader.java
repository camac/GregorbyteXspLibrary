package com.gregorbyte.xsp.extlib.minifier;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.Bundle;

import com.gregorbyte.xsp.GregorbyteActivator;
import com.gregorbyte.xsp.extlib.resources.GregorbyteResourceProvider;
import com.gregorbyte.xsp.extlib.util.GregorbyteUtil;
import com.ibm.commons.util.DoubleMap;

public class GregorbyteControlsLoader extends GregorbyteLoaderExtension {

	public GregorbyteControlsLoader() {
	
	}
	
	@Override
	public Bundle getOSGiBundle() {
		return GregorbyteActivator.getContext().getBundle();
	}
	
	@Override
	public void loadDojoShortcuts(DoubleMap<String, String> aliases,
			DoubleMap<String, String> prefixes) {

		// TODO figure this out!
		super.loadDojoShortcuts(aliases, prefixes);
		
	}

	@Override
	public void loadCSSShortcuts(DoubleMap<String, String> aliases,
			DoubleMap<String, String> prefixes) {

		// TODO figure this out!
		super.loadCSSShortcuts(aliases, prefixes);
	}

	@Override
	public URL getResourceURL(HttpServletRequest request, String name) {
		
		String path = GregorbyteResourceProvider.BUNDLE_RES_PATH_GREGORBYTE+name;		
		return GregorbyteUtil.getResourceURL(getOSGiBundle(), path);
		
	}
	
}
