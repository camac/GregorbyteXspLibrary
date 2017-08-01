package com.gregorbyte.bundleinspector.view;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.gregorbyte.xsp.plugin.Activator;

@SuppressWarnings("restriction")
public class SelectPluginDialog extends FilteredItemsSelectionDialog {

	public static final String IMG_RESOLVED = "/icons/plugin.png";
	public static final String IMG_NOTRESOLVED = "/icons/plugin_disabled.png";

	private static ImageDescriptor resolved;
	private static ImageDescriptor unresolved;

	private static ArrayList<Bundle> resources = new ArrayList<Bundle>();

	static {
		resolved = ImageDescriptor.createFromFile(SelectPluginDialog.class, IMG_RESOLVED);
		unresolved = ImageDescriptor.createFromFile(SelectPluginDialog.class, IMG_NOTRESOLVED);
	}

	public SelectPluginDialog(Shell arg0) {
		super(arg0);
	}

	public SelectPluginDialog(Shell arg0, boolean arg1) {
		super(arg0, arg1);

		setTitle("Select Bundle to Inspect");
		setSelectionHistory(new ResourceSelectionHistory());

		setListLabelProvider(new LabelProvider() {

			private Image resolvedImage = resolved.createImage();
			private Image unresolvedImage = unresolved.createImage();

			@Override
			public Image getImage(Object object) {

				if (object instanceof AbstractBundle) {

					int state = ((AbstractBundle) object).getState();

					if (state >= Bundle.RESOLVED) {
						return resolvedImage;
					} else if (state >= 0) {
						return unresolvedImage;
					}

				}

				return super.getImage(object);
			}

			@Override
			public String getText(Object object) {

				if (object instanceof AbstractBundle) {
					AbstractBundle b = ((AbstractBundle) object);

					return String.format("%s %s", b.getSymbolicName(), b.getBundleData().getVersion());

				}

				return object.toString();
			}

			@Override
			public void dispose() {

				resolvedImage.dispose();
				resolvedImage = null;

				super.dispose();
			}

		});

	}

	public void loadBundles() {

		Bundle[] bundles = AccessController.doPrivileged(new PrivilegedAction<Bundle[]>() {

			@Override
			public Bundle[] run() {

				return Activator.getContext().getBundles();
			}

		});

		for (Bundle bundle : bundles) {
			resources.add(bundle);
		}

	}

	private class ResourceSelectionHistory extends SelectionHistory {

		@Override
		protected Object restoreItemFromMemento(IMemento arg0) {
			return null;
		}

		@Override
		protected void storeItemToMemento(Object arg0, IMemento arg1) {

		}

	}

	@Override
	protected Control createExtendedContentArea(Composite arg0) {
		return null;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {

			@Override
			public boolean matchItem(Object item) {

				if (item instanceof Bundle) {
					return matches(((Bundle) item).getSymbolicName());
				}

				return matches(item.toString());
			}

			@Override
			public boolean isConsistentItem(Object item) {
				return true;
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException {

		progressMonitor.beginTask("Searching", resources.size());

		for (Iterator<Bundle> iter = resources.iterator(); iter.hasNext();) {
			contentProvider.add(iter.next(), itemsFilter);
			progressMonitor.worked(1);
		}

		progressMonitor.done();

	}

	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExample";

	@Override
	protected IDialogSettings getDialogSettings() {

		IDialogSettings settings = null;

		settings = new DialogSettings(DIALOG_SETTINGS);

		return settings;
	}

	@Override
	public String getElementName(Object item) {

		if (item instanceof Bundle) {
			return ((Bundle) item).getSymbolicName();
		}

		return item.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Comparator getItemsComparator() {
		return new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				
				if (o1 instanceof AbstractBundle && o2 instanceof AbstractBundle) {
					
					AbstractBundle b1 = (AbstractBundle)o1;
					AbstractBundle b2 = (AbstractBundle)o2;
										
					int result = b1.getSymbolicName().compareTo(b2.getSymbolicName()); 

					if (result == 0) {
						
						Version v1 = b1.getBundleData().getVersion();
						Version v2 = b2.getBundleData().getVersion();
						
						return v2.compareTo(v1);
						
					} else {
						return result;
					}
					
					
				}
				
				return o1.toString().compareTo(o2.toString());
			}

		};
	}

	@Override
	protected IStatus validateItem(Object arg0) {
		return Status.OK_STATUS;
	}

}
