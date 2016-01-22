package com.gregorbyte.xsp.renderkit.markdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.markdown4j.Markdown4jProcessor;

import com.gregorbyte.xsp.log.GregorbyteLogger;
import com.ibm.commons.log.LogMgr;
import com.ibm.xsp.component.xp.XspInputTextarea;
import com.ibm.xsp.renderkit.html_basic.TextAreaRenderer;

public class MarkdownRenderer extends TextAreaRenderer {

	private static final LogMgr logger = GregorbyteLogger.CONTROLS;

	public MarkdownRenderer() {

		/**
		 * Big Thanks to Martin Rolph of Oval UK for this code
		 * Completely lifted from OvalUK/XPageDemos github
		 */
		
	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

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

	private void encodeMarkdown(FacesContext context, XspInputTextarea md)
			throws IOException {

		logger.info("In Encode Markdown");
		
		ResponseWriter w = context.getResponseWriter();

		w.startElement("span", md);
		writeId(w, context, md);

		Object o = md.getValue();
		String str = md.getValueAsString();

		if (str != null) {

			if (str.startsWith("/")) {

				//InputStream is = context.getExternalContext().getResourceAsStream("TestMarkdown.md");
				InputStream is = context.getExternalContext().getResourceAsStream("/WEB-INF/markdown/Sample.md");
				
				String markdown = processInputStream(is);
				w.write(markdown);
				
			} else {

				String markdown = process(str);
				w.write(markdown);
				
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
			input = input.replaceAll(removeFonts, "$6").replaceAll(removeSpans,
					"$6");

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

}