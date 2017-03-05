package com.gregorbyte.xsp.renderkit.markdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.eclipse.core.runtime.Platform;
import org.markdown4j.Markdown4jProcessor;
import org.osgi.framework.Bundle;

import com.gregorbyte.xsp.log.GregorbyteLogger;
import com.ibm.commons.log.LogMgr;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIPassThroughText;
import com.ibm.xsp.component.xp.XspInputTextarea;
import com.ibm.xsp.renderkit.html_basic.TextAreaRenderer;
import com.ibm.xsp.util.FacesUtil;
import com.ibm.xsp.util.TypedUtil;

public class MarkdownRenderer extends TextAreaRenderer {

	private static final LogMgr logger = GregorbyteLogger.CONTROLS;

	public MarkdownRenderer() {

		/**
		 * Big Thanks to Martin Rolph of Oval UK for this code Completely lifted
		 * from OvalUK/XPageDemos github
		 */

	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

		logger.info("In Encode End");

		if (component instanceof XspInputTextarea) {

			XspInputTextarea md = (XspInputTextarea) component;

			if (isReadOnly(context, md)) {

				encodeMarkdown(context, md);
				return;
			}

		}

		super.encodeEnd(context, component);

	}

	private void encodeMarkdown(FacesContext context, XspInputTextarea md) throws IOException {

		logger.info("In Encode Markdown");

		ResponseWriter w = context.getResponseWriter();

		w.startElement("span", md);
		writeId(w, context, md);

		Object o = md.getValue();
		String str = md.getValueAsString();

		if (StringUtil.isNotEmpty(str)) {

			if (str.startsWith("/") && StringUtil.endsWithIgnoreCase(str, ".md")) {

				InputStream is = null;

				if (StringUtil.startsWithIgnoreCase(str, "/WEB-INF")) {
					is = context.getExternalContext().getResourceAsStream(str);
				} else if (str.split("/").length > 2) {

					String[] bits = str.split("/");

					String bundleId = bits[1];

					String path = str.replace("/" + bundleId, "");

					if (StringUtil.isNotEmpty(bundleId) && StringUtil.isNotEmpty(path)) {

						Bundle bundle = Platform.getBundle(bundleId);

						URL u = getResourceURL(bundle, path);

						try {

							if (u != null) {
								is = u.openStream();
							}

						} catch (Exception e) {

						}

					}

				} else {
					is = context.getExternalContext().getResourceAsStream(str);
				}

				if (is != null) {
					String markdown = processInputStream(is);
					w.write(markdown);
					is.close();
				}

			} else {

				String markdown = process(str);
				w.write(markdown);

			}
		} else {

			List<UIComponent> kids = TypedUtil.getChildren(md);

			for (UIComponent kid : kids) {

				if (kid instanceof UIPassThroughText) {
					String markdown = process(((UIPassThroughText) kid).getText());
					w.write(markdown);
				}

			}

		}

		w.endElement("span");

	}

	public static String processInputStream(InputStream is) {
		try {
			return new Markdown4jProcessor().process(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String process(String input) {

		String output = "";

		try {
			// remove spans and fonts
			String removeFonts = "<(FONT|font)([ ]([a-zA-Z]+)=(\\\"|\')[^\\\"\\\\\']+(\\\"|\'))*[^>]+>([^<]+)(<\\/FONT>|<\\/font>)";
			String removeSpans = "<(SPAN|span)([ ]([a-zA-Z]+)=(\\\"|\')[^\\\"\\\\\']+(\\\"|\'))*[^>]+>([^<]+)(<\\/SPAN>|<\\/span>)";
			input = input.replaceAll(removeFonts, "$6").replaceAll(removeSpans, "$6");

			// look for image embedds
			// String pattern =
			// "(?i)(\\%24\\%24OpenDominoDocument.xsp\\?documentId=.*?)(.+?)(\\&amp;action=editDocument)";
			String pattern = "(?i)\\[youtube\\]\\((.*?)\\)";
			String replace = "<iframe class=\"youtube-player\" type=\"text/html\" width=\"432\" height=\"270\" style=\"max-width:100%;\" src=\"http://www.youtube.com/embed/$1?wmode=opaque\" frameborder=\"0\"></iframe>";
			input = input.replaceAll(pattern, replace);
			// input = input.replaceAll(pattern, "0/$2\\?OpenDocument");
			// ![:youtube 600x400](G-M7ECt3-zY)
			// [[embed url=http://www.youtube.com/watch?v=6YbBmqUnoQM]]

			output = new Markdown4jProcessor().process(input);

			output = output.replace(" /", "/");

		} catch (IOException e) {

			e.printStackTrace();
		}

		return output;
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

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

}