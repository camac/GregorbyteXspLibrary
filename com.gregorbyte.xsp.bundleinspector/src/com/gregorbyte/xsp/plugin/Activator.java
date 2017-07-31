package com.gregorbyte.xsp.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static Activator instance;
	private static BundleContext context;

	public Activator() {
		instance = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}


	public static BundleContext getContext() {
		return context;
	}

}
