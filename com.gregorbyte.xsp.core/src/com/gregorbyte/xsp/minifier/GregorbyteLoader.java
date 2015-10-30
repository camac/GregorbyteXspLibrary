package com.gregorbyte.xsp.minifier;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.gregorbyte.xsp.log.GregorbyteLogger;
import com.gregorbyte.xsp.resources.GregorbyteResourceProvider;
import com.gregorbyte.xsp.util.GregorbyteUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.context.DojoLibrary;
import com.ibm.xsp.minifier.CSSResource;
import com.ibm.xsp.minifier.DojoResource;
import com.ibm.xsp.minifier.ResourceLoader;

public class GregorbyteLoader extends ResourceLoader {

	public static class GregorbyteDojoResource extends UrlDojoResource {

		public GregorbyteDojoResource(DojoLibrary dojoLibrary, String name, URL url) {
			super(dojoLibrary, name, url);
		}	
		
	}
	
	@Override
	public CSSResource getCSSResource(String name, DojoLibrary dojoLibrary)
			throws IOException {

		GregorbyteLogger.RESOURCES.traceDebug("Gbyte has been asked for dojo resource name {0}", name);

		return null;
	}

	@Override
	public DojoResource getDojoResource(String name, DojoLibrary dojoLibrary)
			throws IOException {
	
		GregorbyteLogger.RESOURCES.traceDebug("Gbyte has been asked for dojo resource name {0}", name);
		
		if (name.startsWith("gregorbyte.")) {
			
			List<GregorbyteLoaderExtension> extensions = GregorbyteLoaderExtension.getExtensions();
			
			for (GregorbyteLoaderExtension extension : extensions) {
				DojoResource r = loadDojoResource(name, dojoLibrary, extension);
				if (r != null) {
					return r;
				}
			}
			
		}
		
		return null;
	}

	protected DojoResource loadDojoResource(String name, DojoLibrary dojoLibrary, GregorbyteLoaderExtension ext) {
        String path = GregorbyteResourceProvider.BUNDLE_RES_PATH+StringUtil.replace(name, '.', '/')+".js"; //$NON-NLS-1$
        URL u = GregorbyteUtil.getResourceURL(ext.getOSGiBundle(), path);
        if(u!=null) {
            return new GregorbyteDojoResource(dojoLibrary,name,u);
        }
        return null;
    }
	
}
