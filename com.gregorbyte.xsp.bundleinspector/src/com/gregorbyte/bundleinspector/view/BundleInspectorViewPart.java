package com.gregorbyte.bundleinspector.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class BundleInspectorViewPart extends ViewPart {

	private Composite container = null;
	
	@Override
	public void createPartControl(Composite container) {

		this.container = container;
		
		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);

		GridData searchTextLayoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		searchTextLayoutData.heightHint = 20;
		// searchTextLayoutData.horizontalSpan = 2;
		searchText.setLayoutData(searchTextLayoutData);
		searchText.setMessage("Filter Active JobHub List...");
		
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	
	
}
