package com.gregorbyte.xsp.beans;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.osgi.framework.Bundle;

import com.gregorbyte.xsp.library.BundleInspectorXspLibrary;
import com.gregorbyte.xsp.plugin.Activator;
import com.ibm.commons.util.StringUtil;
import com.ibm.security.pkcs7.DigestedData;

import org.eclipse.core.runtime.internal.adaptor.EclipseAdaptorMsg;
import org.eclipse.core.runtime.internal.adaptor.EclipseCommandProvider;
import org.eclipse.core.runtime.internal.adaptor.MessageHelper;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.framework.internal.core.Framework;
import org.eclipse.osgi.framework.internal.core.OSGi;
import org.eclipse.osgi.framework.internal.core.SystemBundle;
import org.eclipse.osgi.framework.internal.core.SystemBundleActivator;
import org.eclipse.osgi.internal.baseadaptor.SystemBundleData;
import org.eclipse.osgi.internal.resolver.StateHelperImpl;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class BundlesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public String filter = null;
	public String filterLocation = null;

	public Map<Long, BundleDiagnosis> diagnoses = new HashMap<Long, BundleDiagnosis>();

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilterLocation() {
		return filterLocation;
	}

	public void setFilterLocation(String filterLocation) {
		this.filterLocation = filterLocation;
	}

	public BundleDiagnosis getDiagnosis() {
		
		Bundle b = getCurrentBundle();
		
		if (b == null) return null;
		
		return diagnoses.get(b.getBundleId());
		
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

	private Bundle getCurrentBundle() {

		FacesContext fc = FacesContext.getCurrentInstance();
		Object o = FacesContext.getCurrentInstance().getApplication().getVariableResolver().resolveVariable(fc,
				"bundle");

		if (o != null && o instanceof Bundle) {
			return (Bundle) o;
		}

		return null;

	}

	private BundleDescription getBundleDescriptionFromToken(State state, String token) {
		try {
			long id = Long.parseLong(token);
			return state.getBundle(id);
		} catch (NumberFormatException nfe) {
			BundleDescription[] allBundles = state.getBundles(token);
			if (allBundles.length > 0)
				return allBundles[0];
		}
		return null;
	}

	private String getConstraintString(VersionConstraint paramVersionConstraint) {
		VersionRange localVersionRange = paramVersionConstraint.getVersionRange();
		if (localVersionRange == null) {
			return paramVersionConstraint.getName();
		}
		return paramVersionConstraint.getName() + '_' + localVersionRange;
	}

	private String getErrorMessage(VersionConstraint vc) {

		String str = getConstraintString(vc);

		if (vc instanceof ImportPackageSpecification) {
			return "Missing imported package " + str;
		} else if (vc instanceof BundleSpecification) {

			if (((BundleSpecification) vc).isOptional()) {
				return "Missing optionally required bundle " + str;
			} else {
				return "Missing required bundle " + str;
			}

		} else {
			return "Missing host " + str;
		}

	}

	public void diagnose() {

		final Bundle b = getCurrentBundle();

		if (b == null) {
			System.out.println("No Bundle");
			return;
		}

		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {

				BundleContext context = null;
				PlatformAdmin platformAdmin = null;

				// Get System Bundle
				Bundle sb = Activator.getContext().getBundle(0);
				context = sb.getBundleContext();
				ServiceReference[] refs = sb.getRegisteredServices();

				for (ServiceReference serviceReference : refs) {

					Object o = serviceReference.getProperty("objectClass");

					if (o != null && o instanceof String[]) {
						String[] sa = (String[]) o;
						if (sa.length > 0 && StringUtil.endsWithIgnoreCase(sa[0], "PlatformAdmin")) {
							platformAdmin = (PlatformAdmin) context.getService(serviceReference);
						}
					}
				}

				if (platformAdmin == null) {
					System.out.println("Could not find PlatformAdmin");
					return null;
				}

				try {
					State systemState = platformAdmin.getState(false);
					BundleDescription bundle = getBundleDescriptionFromToken(systemState, b.getBundleId() + "");
					if (bundle == null) {
						System.out.println("Could not find bundle");
						return null;
					}

					BundleDiagnosis diagnosis = new BundleDiagnosis();
					diagnoses.put(bundle.getBundleId(), diagnosis);

					diagnosis.setName(bundle.getName());
					diagnosis.setLocation(bundle.getLocation());
					diagnosis.setId(bundle.getBundleId() + "");

					// Unsatisfied
					VersionConstraint[] unsatisfied = platformAdmin.getStateHelper().getUnsatisfiedConstraints(bundle);
					ResolverError[] resolverErrors = platformAdmin.getState(false).getResolverErrors(bundle);

					for (int i = 0; i < resolverErrors.length; i++) {
						if ((resolverErrors[i].getType() & (ResolverError.MISSING_FRAGMENT_HOST
								| ResolverError.MISSING_GENERIC_CAPABILITY | ResolverError.MISSING_IMPORT_PACKAGE
								| ResolverError.MISSING_REQUIRE_BUNDLE)) != 0)
							continue;
						diagnosis.addResolverError(resolverErrors[i].toString());
						diagnosis.print("  "); //$NON-NLS-1$
						diagnosis.println(resolverErrors[i].toString());
					}

					if (unsatisfied.length == 0 && resolverErrors.length == 0) {
						diagnosis.print("  "); //$NON-NLS-1$
						diagnosis.println("No unresolved constraints.");
					}
					if (unsatisfied.length > 0) {
						diagnosis.print("  "); //$NON-NLS-1$
						diagnosis.println("Direct constraints which are unresolved:");
					}
					for (int i = 0; i < unsatisfied.length; i++) {
						diagnosis.print("    "); //$NON-NLS-1$
						VersionConstraint vc = unsatisfied[i];
						String str = getErrorMessage(vc);
						diagnosis.println(str);

					}

					// Unsatisfied Leaves
					VersionConstraint[] unsatisfiedLeaves = platformAdmin.getStateHelper()
							.getUnsatisfiedLeaves(new BundleDescription[] { bundle });

					boolean foundLeaf = false;

					for (int i = 0; i < unsatisfiedLeaves.length; i++) {

						if (unsatisfiedLeaves[i].getBundle() == bundle)
							continue;

						if (!foundLeaf) {
							foundLeaf = true;
							diagnosis.print("  "); //$NON-NLS-1$
							diagnosis.println("Leaf constraints in the dependency chain which are unresolved: ");
						}
						diagnosis.print("    "); //$NON-NLS-1$
						diagnosis.println(unsatisfiedLeaves[i].getBundle().getLocation() + " [" //$NON-NLS-1$
								+ unsatisfiedLeaves[i].getBundle().getBundleId() + "]"); //$NON-NLS-1$
						diagnosis.print("      "); //$NON-NLS-1$
						diagnosis.println(getErrorMessage(unsatisfiedLeaves[i]));
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {

					try {
						ServiceReference platformAdminRef = context.getServiceReference(PlatformAdmin.class.getName());
						context.ungetService(platformAdminRef);
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}

				return null;
			}

		});

	}

}
