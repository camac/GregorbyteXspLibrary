package com.gregorbyte.bundleinspector.view;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.ResolverError;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.VersionConstraint;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.gregorbyte.xsp.beans.BundleDiagnosis;
import com.gregorbyte.xsp.plugin.Activator;
import com.ibm.commons.util.StringUtil;

public class OpenDialogAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	@Override
	public void run(IAction arg0) {

		SelectPluginDialog d = new SelectPluginDialog(window.getShell(), true);
		
		d.loadBundles();
		
		if (d.open() != Window.OK) {
			return;
		}

		Object[] o = d.getResult();

		if (o != null && o.length > 0) {
			
			if (o[0] instanceof Bundle) {
				
				BundleDiagnosis diag = diagnose((Bundle)o[0]);
				MessageDialog.openInformation(window.getShell(), "Bundle Diagnosis", diag.getMessage());
				
			} else {
				System.out.println("not a bundle");
			}
			
		}
		
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow arg0) {
		this.window = arg0;
	}

	public BundleDiagnosis diagnose(final Bundle b) {

		if (b == null) {
			System.out.println("No Bundle");
			return null;
		}

		return AccessController.doPrivileged(new PrivilegedAction<BundleDiagnosis>() {

			@Override
			public BundleDiagnosis run() {

				BundleDiagnosis diagnosis = new BundleDiagnosis();
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
				return diagnosis;

			}

		});

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

}