package com.gregorbyte.xsp.renderkit.phone;

import java.io.IOException;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.gregorbyte.xsp.component.phone.UIPhoneNumber;
import com.ibm.xsp.renderkit.html_basic.InputTextRenderer;

public class PhoneNumberRenderer extends InputTextRenderer {

	@Override
	protected void writeTag(FacesContext arg0, UIInput arg1,
			ResponseWriter rw, String arg3) throws IOException {

		if (arg1 instanceof UIPhoneNumber) {
		
			UIPhoneNumber pn = (UIPhoneNumber)arg1;
			
			if (pn.isValid() && pn.getRegionCode() != null) {
				
				String regionCode = pn.getRegionCode();

				rw.startElement("img", null);
				rw.writeAttribute("src", regionCode + ".png", "flagCode");
				// rw.writeURIAttribute(arg0, arg1, arg2)
				rw.endElement("img");

			}
		}

		super.writeTag(arg0, arg1, rw, arg3);
	}

}
