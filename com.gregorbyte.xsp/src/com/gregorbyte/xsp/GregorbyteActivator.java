package com.gregorbyte.xsp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.gregorbyte.xsp.minifier.GregorbyteControlsLoader;
import com.gregorbyte.xsp.minifier.GregorbyteLoaderExtension;

public class GregorbyteActivator implements BundleActivator {

	public static GregorbyteActivator instance;
	
	private static BundleContext context;
	
	public GregorbyteActivator() {
		instance = this;
		
		GregorbyteLoaderExtension.getExtensions().add(new GregorbyteControlsLoader());
		
	}

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {

		GregorbyteActivator.context = bundleContext;		
		System.out.println("Gregorbyte Extlib: started");
			
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {

		GregorbyteActivator.context = null;
		System.out.println("Gregorbyte ExtLib: stopped");
		
	}

}
