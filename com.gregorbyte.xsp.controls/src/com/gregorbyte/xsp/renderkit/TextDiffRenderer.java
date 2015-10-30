package com.gregorbyte.xsp.renderkit;

import java.io.IOException;
import java.util.LinkedList;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import name.fraser.neil.plaintext.DiffMatchPatch;
import name.fraser.neil.plaintext.DiffMatchPatch.Diff;

import com.gregorbyte.xsp.component.UITextDiff;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.util.HtmlUtil;

public class TextDiffRenderer extends Renderer {

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

		// Don't bother if not rendered
		if (!component.isRendered())
			return;

		UITextDiff diffComp = null;

		if (component instanceof UITextDiff) {
			diffComp = (UITextDiff) component;
		}

		ResponseWriter rw = context.getResponseWriter();

		String from = StringUtil.getNonNullString(diffComp.getFrom());
		String to = StringUtil.getNonNullString(diffComp.getTo());

		DiffMatchPatch dmp = new DiffMatchPatch();

		Float timeout = diffComp.getTimeout();
		
		if (timeout != null) {
			dmp.Diff_Timeout = timeout;
		}
		
		LinkedList<Diff> diffs = dmp.diff_main(from, to);

		String cleanup = diffComp.getCleanup();
		
		if (StringUtil.equals(cleanup, UITextDiff.CLEANUP_SEMANTIC)) {
			dmp.diff_cleanupSemantic(diffs);
			
		} else if (StringUtil.equals(cleanup, UITextDiff.CLEANUP_EFFICIENCY)) {
			
			Short editCost = diffComp.getEditCost();
			
			if (editCost != null) {
				System.out.println("Using edit cost" + editCost);
				dmp.Diff_EditCost = editCost.shortValue();
			}
			
			dmp.diff_cleanupEfficiency(diffs);			
		} 
		
		for (Diff diff : diffs) {
			renderDiff(rw, diffComp, diff);
		}

	}

	private void renderDiff(ResponseWriter rw, UITextDiff diffComp, Diff diff)
			throws IOException {

		rw.startElement(HtmlUtil.SPAN, null);

		String style = diffComp.getStyle(diff);
		String styleClass = diffComp.getStyleClass(diff);

		if (StringUtil.isNotEmpty(style)) {
			rw.writeAttribute("style", style, null);
		}
		if (StringUtil.isNotEmpty(styleClass)) {
			rw.writeAttribute("styleClass", styleClass, null);
		}

		rw.writeText(diff.text, null);

		rw.endElement(HtmlUtil.SPAN);

	}

}
