package com.gregorbyte.bundleinspector.view;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.osgi.framework.Bundle;

import com.gregorbyte.xsp.plugin.Activator;
import com.ibm.commons.util.StringUtil;

public class SelectPluginDialog extends FilteredItemsSelectionDialog {

	private static ArrayList<Bundle> resources = new ArrayList<Bundle>();

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

	public SelectPluginDialog(Shell arg0, boolean arg1) {
		super(arg0, arg1);

		setTitle("Select Bundle to Inspect");
		setSelectionHistory(new ResourceSelectionHistory());

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

	public SelectPluginDialog(Shell arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
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

		for (Iterator iter = resources.iterator(); iter.hasNext();) {
			contentProvider.add(iter.next(), itemsFilter);
			progressMonitor.worked(1);
		}

		progressMonitor.done();

	}

	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExample";

	@Override
	protected IDialogSettings getDialogSettings() {

		IDialogSettings settings = Activator.instance.getDialogSettings().getSection(DIALOG_SETTINGS);

		settings = new DialogSettings(DIALOG_SETTINGS);
		
		return settings;
	}

	@Override
	public String getElementName(Object item) {
		
		if (item instanceof Bundle) {
			return ((Bundle)item).getSymbolicName();
		}
		
		return item.toString();
	}

	@Override
	protected Comparator getItemsComparator() {
		return new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}

		};
	}

	@Override
	protected IStatus validateItem(Object arg0) {
		return Status.OK_STATUS;
	}
	
	

}
