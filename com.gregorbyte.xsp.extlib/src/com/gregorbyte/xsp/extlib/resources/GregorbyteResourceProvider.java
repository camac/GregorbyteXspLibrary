package com.gregorbyte.xsp.extlib.resources;

import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.gregorbyte.xsp.extlib.log.GregorbyteLogger;
import com.gregorbyte.xsp.extlib.minifier.GregorbyteLoaderExtension;
import com.ibm.xsp.webapp.FacesResourceServlet;
import com.ibm.xsp.webapp.resources.URLResourceProvider;

public class GregorbyteResourceProvider extends URLResourceProvider {

	public static final String BUNDLE_RES_PATH = "/resources/web/";
	public static final String BUNDLE_RES_PATH_GREGORBYTE = "/resources/web/gregorbyte/";

	public static final String GREGORBYTE_PREFIX = ".gregorbyte";

	public static final String RESOURCE_PATH = FacesResourceServlet.RESOURCE_PREFIX
			+ GREGORBYTE_PREFIX + "/";

	public static final String DOJO_PATH = FacesResourceServlet.RESOURCE_PREFIX
			+ GREGORBYTE_PREFIX;

	public GregorbyteResourceProvider() {
		super(GREGORBYTE_PREFIX);
	}

	protected boolean shouldCacheResources() {
		return false;
	}

	@Override
	protected URL getResourceURL(HttpServletRequest request, String name) {

		GregorbyteLogger.RESOURCES.info("Gregorbyte has been asked for {0}",
				name);

		List<GregorbyteLoaderExtension> extensions = GregorbyteLoaderExtension
				.getExtensions();
		int size = extensions.size();
		for (int i = 0; i < size; i++) {
			URL url = extensions.get(i).getResourceURL(request, name);
			if (url != null) {
				return url;
			}
		}
		return null;
	}

}
