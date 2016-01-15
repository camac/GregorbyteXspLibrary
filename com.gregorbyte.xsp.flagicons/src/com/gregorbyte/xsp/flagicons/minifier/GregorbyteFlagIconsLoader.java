package com.gregorbyte.xsp.flagicons.minifier;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.Bundle;

import com.gregorbyte.xsp.flagicons.plugin.GregorbyteFlagIconsActivator;
import com.gregorbyte.xsp.minifier.GregorbyteLoaderExtension;
import com.gregorbyte.xsp.resources.GregorbyteResourceProvider;
import com.gregorbyte.xsp.util.GregorbyteUtil;

public class GregorbyteFlagIconsLoader extends GregorbyteLoaderExtension {

	@Override
	public Bundle getOSGiBundle() {
		return GregorbyteFlagIconsActivator.getContext().getBundle();
	}
	
	@Override
	public URL getResourceURL(HttpServletRequest request, String name) {
		
		String path = GregorbyteResourceProvider.BUNDLE_RES_PATH_GREGORBYTE+name;		
		return GregorbyteUtil.getResourceURL(getOSGiBundle(), path);
		
	}

}
