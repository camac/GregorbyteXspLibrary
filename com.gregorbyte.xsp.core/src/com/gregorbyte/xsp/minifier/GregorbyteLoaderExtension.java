package com.gregorbyte.xsp.minifier;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.Bundle;

import com.ibm.commons.util.DoubleMap;

public abstract class GregorbyteLoaderExtension {

	private static List<GregorbyteLoaderExtension> extensions = new ArrayList<GregorbyteLoaderExtension>();

	public static List<GregorbyteLoaderExtension> getExtensions() {
		return extensions;
	}

	protected GregorbyteLoaderExtension() {
	}

	public abstract Bundle getOSGiBundle();

	public void loadDojoShortcuts(DoubleMap<String, String> aliases,
			DoubleMap<String, String> prefixes) {
	}

	public void loadCSSShortcuts(DoubleMap<String, String> aliases,
			DoubleMap<String, String> prefixes) {
	}

	public URL getResourceURL(HttpServletRequest request, String name) {
		return null;
	}

}
