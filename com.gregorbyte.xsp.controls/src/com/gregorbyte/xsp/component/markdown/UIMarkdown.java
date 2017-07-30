package com.gregorbyte.xsp.component.markdown;

import java.io.IOException;

import javax.faces.context.FacesContext;

import com.ibm.xsp.component.xp.XspInputTextarea;

public class UIMarkdown extends XspInputTextarea {

	public static final String RENDERER_TYPE = "Text.Markdown";

	public UIMarkdown() {
		setRendererType(RENDERER_TYPE);
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
	}

	@Override
	public void restoreState(FacesContext context, Object state) {

		Object[] values = (Object[]) state;

		super.restoreState(context, values[0]);

	}

	@Override
	public Object saveState(FacesContext context) {

		Object[] values = new Object[1];

		values[0] = super.saveState(context);

		return values;

	}

}