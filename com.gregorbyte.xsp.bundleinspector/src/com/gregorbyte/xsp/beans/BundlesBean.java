package com.gregorbyte.xsp.beans;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;

import com.gregorbyte.xsp.plugin.Activator;
import com.ibm.commons.util.StringUtil;

public class BundlesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public String filter = null;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public List<Bundle> getBundles() {

		Bundle[] bundles = AccessController.doPrivileged(new PrivilegedAction<Bundle[]>() {

			@Override
			public Bundle[] run() {

				return Activator.getContext().getBundles();
			}

		});

		List<Bundle> bundleList = new ArrayList<Bundle>();

		for (Bundle bundle : bundles) {

			if (StringUtil.isEmpty(filter) || StringUtil.getNonNullString(bundle.getSymbolicName()).contains(filter)) {
				bundleList.add(bundle);
			}
		}

		return bundleList;

	}

}
