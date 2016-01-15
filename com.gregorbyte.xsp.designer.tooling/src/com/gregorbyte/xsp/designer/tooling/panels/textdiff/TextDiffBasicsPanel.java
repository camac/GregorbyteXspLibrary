package com.gregorbyte.xsp.designer.tooling.panels.textdiff;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.gregorbyte.xsp.designer.tooling.constants.GregorbyteAttrNames;
import com.ibm.commons.iloader.node.lookups.api.StringLookup;
import com.ibm.commons.swt.data.controls.DCCompositeText;
import com.ibm.designer.ide.xsp.components.api.panels.XSPBasicsPanel;

public class TextDiffBasicsPanel extends XSPBasicsPanel {

	public TextDiffBasicsPanel(Composite parent, int style) {
		super(parent, style);
	}

	@SuppressWarnings("unused")
	@Override
	protected void createGroupBoxContents(Group groupBox) {

		int cols = getNumGroupBoxColumns();
		GridData data = createControlGDNoWidth(cols);
		data.horizontalIndent = 0;

		Label l = createLabel("Edit Cost:", null); // $NLX-NavigatorBasicsPanel.Expandtolevel-1$
		Control c = createDCTextComputed(GregorbyteAttrNames.GB_ATTR_EDITCOST,
				createControlGDNoWidth(cols - 1));
		((DCCompositeText) c).setCols(5);

		l = createLabel("Cleanup:", null); // $NLX-NavigatorBasicsPanel.Expandeffect-1$
		c = createComboComputed(GregorbyteAttrNames.GB_ATTR_CLEANUP,
				new StringLookup(new String[] { "none", "efficiency",
						"semantic" }, new String[] { "No Cleanup",
						"Efficiency", "Semantic" }),
				createControlGDDefWidth(cols - 1), true, true); // $NON-NLS-1$
																// $NLX-NavigatorBasicsPanel.Wipe-2$

	}

}
