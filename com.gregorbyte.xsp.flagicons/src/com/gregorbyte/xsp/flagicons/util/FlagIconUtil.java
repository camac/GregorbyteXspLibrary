package com.gregorbyte.xsp.flagicons.util;

import java.io.IOException;

import javax.faces.context.ResponseWriter;

import com.gregorbyte.xsp.resources.GregorbyteResourceProvider;
import com.ibm.commons.util.StringUtil;

public class FlagIconUtil {

	public static void writeFlagIcon(ResponseWriter w, String countryCode)
			throws IOException {

		w.startElement("img", null);
				
		w.writeAttribute("src", getFlagIconUrl(countryCode), null);
		
		w.endElement("img");
		
	}
	
	public static String getFlagIconUrl(String countryCode) {
		
		if (StringUtil.isEmpty(countryCode)) return null;
		
		String basepath = "/xsp" + GregorbyteResourceProvider.RESOURCE_PATH;		
		return basepath + "flags/" + countryCode.toLowerCase() + ".png";		
		
	}

}
