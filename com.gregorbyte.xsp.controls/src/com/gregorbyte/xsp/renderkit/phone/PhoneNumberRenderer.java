package com.gregorbyte.xsp.renderkit.phone;

import java.io.IOException;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.gregorbyte.xsp.component.phone.UIPhoneNumber;
import com.gregorbyte.xsp.flagicons.util.FlagIconUtil;
import com.ibm.xsp.renderkit.html_basic.InputTextRenderer;

public class PhoneNumberRenderer extends InputTextRenderer {

	@Override
	protected void writeTag(FacesContext context, UIInput component,
			ResponseWriter rw, String arg3) throws IOException {

		if (component instanceof UIPhoneNumber) {
		
			UIPhoneNumber pn = (UIPhoneNumber)component;
			
			if (pn.isValid() && pn.getRegionCode() != null) {
				
				String regionCode = pn.getRegionCode();

				FlagIconUtil.writeFlagIcon(rw, regionCode);

			}
		}

		super.writeTag(context, component, rw, arg3);
	}

	
	
}
