package com.gregorbyte.xsp.util;

import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;

public class GregorbyteUtil {

	public static URL getResourceURL(Bundle bundle, String path) {

		int fileNameIndex = path.lastIndexOf('/');
		String fileName = path.substring(fileNameIndex + 1);
		path = path.substring(0, fileNameIndex + 1);

		// see http://www.osgi.org/javadoc/r4v42/org/osgi/framework/Bundle.html
		// #findEntries%28java.lang.String,%20java.lang.String,%20boolean%29
		Enumeration<?> urls = bundle.findEntries(path, fileName, false/* recursive */);
		if (null != urls && urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			if (null != url) {
				return url;
			}
		}
		return null; // no match, 404 not found.
	}

	// Copied this from ExtLibUtil just to avoid a dependancy on it for now
	public static String concatStyleClasses(String s1, String s2) {
		if (StringUtil.isNotEmpty(s1)) {
			if (StringUtil.isNotEmpty(s2)) {
				return s1 + " " + s2;
			}
			return s1;
		} else {
			if (StringUtil.isNotEmpty(s2)) {
				return s2;
			}
			return "";
		}
	}

}
