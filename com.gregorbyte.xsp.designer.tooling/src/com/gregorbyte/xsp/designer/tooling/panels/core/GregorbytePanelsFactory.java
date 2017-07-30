package com.gregorbyte.xsp.designer.tooling.panels.core;

import org.eclipse.swt.widgets.Composite;

import com.gregorbyte.xsp.designer.tooling.panels.textdiff.TextDiffBasicsPanel;
import com.gregorbyte.xsp.designer.tooling.panels.textdiff.TextDiffNodesPanel;
import com.gregorbyte.xsp.designer.tooling.panels.textdiff.TextDiffOthersPanel;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.xsp.api.panels.AbstractPanelsFactory;
import com.ibm.designer.domino.xsp.api.panels.XPagesPanelDescriptor;

public class GregorbytePanelsFactory extends AbstractPanelsFactory implements
		GregorbytePanelIds {

	@Override
	protected Class<? extends Composite> getPanelControlClass(
			XPagesPanelDescriptor descriptor) {

		if (descriptor != null) {

			String id = descriptor.getId();

			if (StringUtil.equals(id, TEXTDIFF_BASICS_PANEL)) {
				return TextDiffBasicsPanel.class;
			}

			if (StringUtil.equals(id, TEXTDIFF_NODES_PANEL)) {
				return TextDiffNodesPanel.class;
			}

			if (StringUtil.equals(id, TEXTDIFF_OTHERS_PANEL)) {
				return TextDiffOthersPanel.class;
			}

		}

		return null;
	}

}
