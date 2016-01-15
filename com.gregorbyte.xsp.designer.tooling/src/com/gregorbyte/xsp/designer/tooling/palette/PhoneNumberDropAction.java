package com.gregorbyte.xsp.designer.tooling.palette;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.designer.domino.xsp.api.palette.XPagesPaletteDropActionDelegate;

public class PhoneNumberDropAction extends XPagesPaletteDropActionDelegate {

	@Override
	protected Element createElement(Document arg0, String arg1) {

		System.out.println("Phone Number Drop Action");

		return super.createElement(arg0, arg1);
	}

	
}
